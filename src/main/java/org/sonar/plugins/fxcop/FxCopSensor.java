/*
 * SonarQube FxCop Library
 * Copyright (C) 2014-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.fxcop;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.Optional;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.config.Configuration;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

@SuppressWarnings("deprecation")
public class FxCopSensor implements Sensor {

  private static final String CUSTOM_RULE_KEY = "CustomRuleTemplate";
  private static final String CUSTOM_RULE_CHECK_ID_PARAMETER = "CheckId";
  private static final Logger LOG = Loggers.get(FxCopSensor.class);
  private String altSlnFile;

  private final FxCopConfiguration fxCopConf;

  public FxCopSensor(FxCopConfiguration fxCopConf) {
    this.fxCopConf = fxCopConf;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.name("FxCop (" + fxCopConf.languageKey() + ")")
      .createIssuesForRuleRepository(fxCopConf.repositoryKey())
      .onlyOnLanguage(fxCopConf.languageKey())
      .onlyOnFileType(Type.MAIN);
  }

  @Override
  public void execute(SensorContext context) {
    if (!System.getProperty("os.name").startsWith("Windows")) {
      LOG.debug("Skipping FxCop on non Windows OS");
      return;
    }
    executeImpl(context);
  }

void executeImpl(SensorContext context) {
	getAlternativeSlnPath(context);
    fxCopConf.setAlternativeSln(this.altSlnFile);
    Configuration config = context.config();
    if (config==null)throw new IllegalArgumentException("Context is not configured");
    if (!fxCopConf.checkProperties(config)) {
      LOG.warn("Skipping FxCop, either the report file or the assembly is missing");
      return;
    }
    analyse(new FxCopRulesetWriter(), new FxCopReportParser(), new FxCopExecutor(), context);
}

  @VisibleForTesting
  void analyse(FxCopRulesetWriter writer, FxCopReportParser parser, FxCopExecutor executor, SensorContext context) {
	  
	  
	  Configuration settings = context.config();

    File reportFile;
    String reportPath = getReportPath(settings);
      
    if (reportPath == null) {
      reportFile = executeFxCop(writer, executor, context, settings);
    } else {
      LOG.debug("Using the provided FxCop report" + reportPath);
      reportFile = new File(reportPath);
    }

    parseReportFile(parser, context, reportFile);
  }

private String getReportPath(Configuration settings) {
	String reportPath = null;
    String reportKey = fxCopConf.reportPathPropertyKey();
    if (reportKey != null) {
    	Optional<String> reportOptinal = settings.get(reportKey);
    	if (reportOptinal.isPresent()) reportPath = reportOptinal.get();
    }
	return reportPath;
}

String getAlternativeSlnPath(SensorContext context) {
	try {
		  File baseDir = context.fileSystem().baseDir();
		  LOG.info("Base Dir: " + baseDir);
		  altSlnFile = getSlnNameByProperty(context, baseDir);
		  if (altSlnFile == null) {
			  altSlnFile = getSlnFileFromContextPath(baseDir);
		  }
		  return altSlnFile;
	  } catch (Exception ex){
		  LOG.warn("Unable to analyse working directory: " + ex.getMessage());
		  return null;
	  }
	
}

private String getSlnFileFromContextPath(File baseDir) {
	FilenameFilter fileNameFilter = new FilenameFilter() {
		   
	        @Override
	        public boolean accept(File dir, String name) {
	           if(name.lastIndexOf('.')>0) {
	           
	              // get last index for '.' char
	              int lastIndex = name.lastIndexOf('.');
	              
	              // get extension
	              String str = name.substring(lastIndex);
	              
	              // match path name extension
	              if(str.equals(".sln")) {
	                 return true;
	              }
	           }
	           
	           return false;
	        }
	     };
	  
	  File newAltFile = new File(baseDir.getAbsolutePath());
	  newAltFile = getSlnFromPath(baseDir, fileNameFilter, newAltFile);
	  if (isFileSet(newAltFile)){
		  newAltFile = getSlnFromPath(baseDir.getParentFile(), fileNameFilter, newAltFile);
	  }
	  return newAltFile.getAbsolutePath();
}

private File getSlnFromPath(File baseDir, FilenameFilter fileNameFilter, File newAltFile) {
	File currentFile = newAltFile;
	for (String f: baseDir.list(fileNameFilter)){
		  if (isFileSet(currentFile) || isOldSlnTest(newAltFile)) {
			  currentFile=new File(baseDir, f);
		  } 
	  }
	return currentFile;
}

private boolean isOldSlnTest(File newAltFile) {
	return newAltFile.getAbsolutePath().toLowerCase().contains("test") || newAltFile.getAbsolutePath().toLowerCase().contains("sample");
}

private boolean isFileSet(File currentFile) {
	return currentFile.isDirectory() || !currentFile.exists();
}

  private String getSlnNameByProperty(SensorContext context, File baseDir) {
	  try {
		Optional<String> slnName = context.config().get("sonar.dotnet.visualstudio.solution.file");
		if (!slnName.isPresent() || slnName.get().isEmpty()){
			LOG.info("sonar.dotnet.visualstudio.solution.file not set");
			return null;
		}
		File slnFile = new File(slnName.get());
		if (slnFile.exists()) {
			return slnFile.getAbsolutePath();
		}else {
		  slnFile = new File(baseDir,slnName.get());
		  if (slnFile.exists()) {			  
			  return slnFile.getAbsolutePath();
		  }else {
			  LOG.warn("Not Found SLN: " + slnFile.getAbsolutePath());
			  return null;
		  }
	    }
	  }
	  catch (Exception ex){
		  LOG.warn("Error calculate SLN path by 'sonar.dotnet.visualstudio.solution.file': " + ex.getMessage());
		  return null;
	  }
	  
  }

  private File executeFxCop(FxCopRulesetWriter writer, FxCopExecutor executor, SensorContext context, Configuration settings) {
	File reportFile;
	String workDirPath = context.fileSystem().workDir().getAbsolutePath();
	workDirPath = trimWorkdir(settings, workDirPath);
	
	File rulesetFile = new File(workDirPath, "fxcop-sonarqube.ruleset");
      writer.write(enabledRuleConfigKeys(context.activeRules()), rulesetFile);

      reportFile = new File(workDirPath, "fxcop-report.xml");

      String target = getTargetForSetting(settings);
      
      
      executor.setExecutable(settings.get(fxCopConf.fxCopCmdPropertyKey()).orElse(null));
      executor.setTimeout(settings.getInt(fxCopConf.timeoutPropertyKey()).orElse(60));
      executor.setAspnet(settings.getBoolean(fxCopConf.aspnetPropertyKey()).orElse(false));
      
           
      executor.execute(target,
        rulesetFile, reportFile, 
        splitOnCommas(settings.get(fxCopConf.directoriesPropertyKey())), splitOnCommas(settings.get(fxCopConf.referencesPropertyKey())));
	return reportFile;
  }

public static String trimWorkdir(Configuration settings, String workDirPath) {
	Optional<String> projectKey = settings.get("sonar.projectKey");
	if (projectKey.isPresent() && !projectKey.get().isEmpty() && projectKey.get().indexOf(':') >= 0/*&& projectKey.matches("[a-zA-Z0-9_-]:[a-zA-Z0-9_-]:[a-zA-Z0-9_-]")*/){
		workDirPath = workDirPath.replace(projectKey.get().replace(":", ""), projectKey.get().substring(projectKey.get().indexOf(':')));
        workDirPath = workDirPath.replaceAll("^:", "").replace(":[^\\]", "");
		LOG.info("Project key: " + projectKey);
		LOG.info("Work Dir: " + workDirPath);
		File workDir = new File(workDirPath);
		if (!workDir.exists()){
			workDir.mkdirs();
		}
	}
	return workDirPath;
}

  private void parseReportFile(FxCopReportParser parser, SensorContext context, File reportFile) {
	  int count = 0;
	for (FxCopIssue issue : parser.parse(reportFile)) {
      String absolutePath = getSourceFileAbsolutePath(issue);

      InputFile inputFile = null;
      if (absolutePath != null) {
        inputFile = context.fileSystem().inputFile(context.fileSystem().predicates().hasAbsolutePath(absolutePath));
        if (inputFile == null && (absolutePath.endsWith(".cs") || absolutePath.endsWith(".vb"))) {
          LOG.debug("Ignoring issue on file '{}' that is not indexed and was probably excluded", absolutePath);
          continue;
        }
      }

      NewIssue newIssue = context.newIssue()
        .forRule(RuleKey.of(fxCopConf.repositoryKey(), ruleKey(issue.ruleConfigKey(), context.activeRules())));
      if (inputFile != null) {
        NewIssueLocation location = newIssue.newLocation()
          .on(inputFile)
          .message(issue.message());
        Integer line = fxCopToSonarQubeLine(issue.line());
        if (line != null) {
          location.at(inputFile.selectLine(line));
        }
        newIssue.at(location);
      } else {
        NewIssueLocation location = newIssue.newLocation()
          .on(context.module())
          .message(createMessageLocation(absolutePath, issue.line()) + issue.message());
        newIssue.at(location);
      } 

      newIssue.save();
      count ++;
    }
	LOG.info("FxCop found "+ count+" issue(s).");
  }

String getTargetForSetting(Configuration settings) {
	String target = null;
      
      if (settings.hasKey(fxCopConf.assemblyPropertyKey())){
    	  target = settings.get(fxCopConf.assemblyPropertyKey()).orElse(null);
      }
      else if (settings.hasKey(fxCopConf.slnFilePropertyKey())) { 
    	  FxCopProjectGenerator gen = new FxCopProjectGenerator();
    	  String sln = settings.get(fxCopConf.slnFilePropertyKey()).orElse(null);
    	  if (sln!=null) target = gen.generate(sln);
      }
      else { 
    	  target = settings.get(fxCopConf.projectFilePropertyKey()).orElse(null);
      }
	return target;
  }

  private static String createMessageLocation(@Nullable String absolutePath,@Nullable Integer line) {
    String messageLocation = "";
    if (absolutePath != null) {
      messageLocation += absolutePath;

      if (line != null) {
        messageLocation += " line " + line;
      }

      messageLocation += ": ";
    }
    return messageLocation;
  }

  @CheckForNull
  private static Integer fxCopToSonarQubeLine(@Nullable Integer fxcopLine) {
    if (fxcopLine == null) {
      return null;
    }
    return fxcopLine <= 0 ? null : fxcopLine;
  }

  private static List<String> splitOnCommas(Optional<String> property) {
    if (!property.isPresent()) {
      return ImmutableList.of();
    } else {
      return ImmutableList.copyOf(Splitter.on(",").trimResults().omitEmptyStrings().split(property.get()));
    }
  }

  @CheckForNull
  private static String getSourceFileAbsolutePath(FxCopIssue issue) {
    if (issue.path() == null || issue.file() == null) {
      return null;
    }

    File file = new File(new File(issue.path()), issue.file());
    return file.getAbsolutePath();
  }

  private List<String> enabledRuleConfigKeys(ActiveRules activeRules) {
    ImmutableList.Builder<String> builder = ImmutableList.builder();
    for (ActiveRule activeRule : activeRules.findByRepository(fxCopConf.repositoryKey())) {
      if (!CUSTOM_RULE_KEY.equals(activeRule.ruleKey().rule())) {
        String effectiveConfigKey = activeRule.internalKey();
        if (effectiveConfigKey == null) {
          effectiveConfigKey = activeRule.param(CUSTOM_RULE_CHECK_ID_PARAMETER);
          continue;
        }

        builder.add(effectiveConfigKey);
      }
    }
    return builder.build();
  }

  private String ruleKey(String ruleConfigKey, ActiveRules activeRules) {
    for (ActiveRule activeRule : activeRules.findByRepository(fxCopConf.repositoryKey())) {
      if (ruleConfigKey.equals(activeRule.internalKey()) || ruleConfigKey.equals(activeRule.param(CUSTOM_RULE_CHECK_ID_PARAMETER))) {
        return activeRule.ruleKey().rule();
      }
    }

    throw new IllegalStateException(
      "Unable to find the rule key corresponding to the rule config key \"" + ruleConfigKey + "\" in repository \"" + fxCopConf.repositoryKey() + "\".");
  }

}
