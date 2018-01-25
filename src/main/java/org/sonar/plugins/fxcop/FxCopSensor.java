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
import java.util.List;
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
import org.sonar.api.config.Settings;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class FxCopSensor implements Sensor {

  private static final String CUSTOM_RULE_KEY = "CustomRuleTemplate";
  private static final String CUSTOM_RULE_CHECK_ID_PARAMETER = "CheckId";
  private static final Logger LOG = Loggers.get(FxCopSensor.class);

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
    if (!fxCopConf.checkProperties(context.settings())) {
      LOG.warn("Skipping FxCop, either the report file or the assembly is missing");
      return;
    }
    analyse(new FxCopRulesetWriter(), new FxCopReportParser(), new FxCopExecutor(), context);
  }

  @VisibleForTesting
  void analyse(FxCopRulesetWriter writer, FxCopReportParser parser, FxCopExecutor executor, SensorContext context) {
    Settings settings = context.settings();

    File reportFile;
    String reportPath = settings.getString(fxCopConf.reportPathPropertyKey());
    if (reportPath == null) {
      reportFile = executeFxCop(writer, executor, context, settings);
    } else {
      LOG.debug("Using the provided FxCop report" + reportPath);
      reportFile = new File(reportPath);
    }

    parseReportFile(parser, context, reportFile);
  }

  private File executeFxCop(FxCopRulesetWriter writer, FxCopExecutor executor, SensorContext context, Settings settings) {
	File reportFile;
	File rulesetFile = new File(context.fileSystem().workDir(), "fxcop-sonarqube.ruleset");
      writer.write(enabledRuleConfigKeys(context.activeRules()), rulesetFile);

      reportFile = new File(context.fileSystem().workDir(), "fxcop-report.xml");

      String target = getTargetForSetting(settings);
      
      
      executor.setExecutable(settings.getString(fxCopConf.fxCopCmdPropertyKey()));
      executor.setTimeout(settings.getInt(fxCopConf.timeoutPropertyKey()));
      executor.setAspnet(settings.getBoolean(fxCopConf.aspnetPropertyKey()));
      
      executor.execute(target,
        rulesetFile, reportFile, 
        splitOnCommas(settings.getString(fxCopConf.directoriesPropertyKey())), splitOnCommas(settings.getString(fxCopConf.referencesPropertyKey())));
	return reportFile;
  }

  private void parseReportFile(FxCopReportParser parser, SensorContext context, File reportFile) {
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
    }
  }

  private String getTargetForSetting(Settings settings) {
	String target = null;
      
      if (settings.hasKey(fxCopConf.assemblyPropertyKey())){
    	  target = settings.getString(fxCopConf.assemblyPropertyKey());
      }
      else if (settings.hasKey(fxCopConf.slnFilePropertyKey())) { 
    	  FxCopProjectGenerator gen = new FxCopProjectGenerator();
    	  target = gen.generate(settings.getString(fxCopConf.slnFilePropertyKey()));
      }
      else { 
    	  target = settings.getString(fxCopConf.projectFilePropertyKey());
      }
	return target;
  }

  private static String createMessageLocation(String absolutePath, Integer line) {
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

  private static List<String> splitOnCommas(@Nullable String property) {
    if (property == null) {
      return ImmutableList.of();
    } else {
      return ImmutableList.copyOf(Splitter.on(",").trimResults().omitEmptyStrings().split(property));
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
