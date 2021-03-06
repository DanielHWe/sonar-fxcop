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

import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;


public class FxCopConfiguration {

  private static final String IS_NOT_PRESENT = "\" is not present.";
private static final String PROVIDED_BY_THE_PROPERTY = "\" provided by the property \"";
private static final String DEPRECATED_FXCOPCMD_PATH_PROPERTY_KEY = "sonar.fxcop.installDirectory";
  private static final String DEPRECATED_TIMEOUT_MINUTES_PROPERTY_KEY = "sonar.fxcop.timeoutMinutes";
  private static final String MISSING_SCAN_DEFINITION_TEXT = "FxCop plugin missed the definition of what to scan. Please set one of folowing properties:"+
  		"sonar.cs.fxcop.assembly, sonar.cs.fxcop.project, sonar.cs.fxcop.reportPath or sonar.cs.fxcop.slnFile";
  private static final Logger LOG = Loggers.get(FxCopExecutor.class);

  private final String languageKey;
  private final String repositoryKey;
  private final String assemblyPropertyKey;
  private final String projectFilePropertyKey;
  private final String slnFilePropertyKey;
  private String fxCopCmdPropertyKey;
  private String timeoutPropertyKey;
  private int assemblyCount;
  private final String aspnetPropertyKey;
  private final String directoriesPropertyKey;
  private final String referencesPropertyKey;
  private final String reportPathPropertyKey;
private String alternativeSlnFile;


public FxCopConfiguration(String languageKey, String repositoryKey, String assemblyPropertyKey, 
		String projectFilePropertyKey, String slnFilePropertyKey, String fxCopCmdPropertyKey, 
		String timeoutPropertyKey, String aspnetPropertyKey,
	    String directoriesPropertyKey, String referencesPropertyKey,
	    String reportPathPropertyKey) {
	    this.languageKey = languageKey;
	    this.repositoryKey = repositoryKey;
	    this.assemblyPropertyKey = assemblyPropertyKey;
	    this.projectFilePropertyKey = projectFilePropertyKey;
	    this.slnFilePropertyKey = slnFilePropertyKey;
	    this.fxCopCmdPropertyKey = fxCopCmdPropertyKey;
	    this.timeoutPropertyKey = timeoutPropertyKey;
	    this.aspnetPropertyKey = aspnetPropertyKey;
	    this.directoriesPropertyKey = directoriesPropertyKey;
	    this.referencesPropertyKey = referencesPropertyKey;
	    this.reportPathPropertyKey = reportPathPropertyKey;
	  }

  public String languageKey() {
    return languageKey;
  }

  public String repositoryKey() {
    return repositoryKey;
  }

  public String assemblyPropertyKey() {
    return assemblyPropertyKey;
  }
  
  public String projectFilePropertyKey() {
	    return projectFilePropertyKey;
	  }
  
  public String slnFilePropertyKey() {
	    return slnFilePropertyKey;
	  }

  public String fxCopCmdPropertyKey() {
    return fxCopCmdPropertyKey;
  }

  public String timeoutPropertyKey() {
    return timeoutPropertyKey;
  }

  public String aspnetPropertyKey() {
    return aspnetPropertyKey;
  }

  public String directoriesPropertyKey() {
    return directoriesPropertyKey;
  }

  public String referencesPropertyKey() {
    return referencesPropertyKey;
  }

  public String reportPathPropertyKey() {
    return reportPathPropertyKey;
  }

  public boolean checkProperties(Configuration settings) {
	    if (settings.hasKey(reportPathPropertyKey)) {
	      checkReportPathProperty(settings);
	    } else {
	    	checkScanProperties(settings);
	    }
	    return true;
	  }

private void checkScanProperties(Configuration settings) {
	if (isNoScanOptionSet(settings)){
		LOG.warn(MISSING_SCAN_DEFINITION_TEXT);
		if (this.alternativeSlnFile != null && !this.alternativeSlnFile.isEmpty()) {
			LOG.warn("Use default sln file found: " + this.alternativeSlnFile);
			
		} else {
			LOG.error("No possible default found sln, please specify.");
			throw new IllegalArgumentException(MISSING_SCAN_DEFINITION_TEXT);
		}
	}
	
	if (isProjectFileScanOptionSet(settings)) 
	{ 
		checkMandatoryProperties(settings); 
		checkProjectFileProperty(settings);
	    checkFxCopCmdPathProperty(settings);
	    checkTimeoutProeprty(settings);
	} 
	else if (isSolutionFileScanOptionSet(settings)) 
	{ 
		checkMandatoryProperties(settings);
	    checkSlnProperty(settings);
	    checkProjectFileProperty(settings);
	    checkFxCopCmdPathProperty(settings);
	    checkTimeoutProeprty(settings);
	} 
	else {
	  checkMandatoryProperties(settings);
	  checkAssemblyProperty(settings);
	  checkProjectFileProperty(settings);
	  checkFxCopCmdPathProperty(settings);
	  checkTimeoutProeprty(settings);
	}
}

private boolean isSolutionFileScanOptionSet(Configuration settings) {
	return !settings.hasKey(assemblyPropertyKey) && 
			!settings.hasKey(projectFilePropertyKey)&& 
			settings.hasKey(slnFilePropertyKey);
}

private boolean isProjectFileScanOptionSet(Configuration settings) {
	return !settings.hasKey(assemblyPropertyKey) && 
		settings.hasKey(projectFilePropertyKey)&& 
		!hasSlnFile(settings);
}

private boolean isNoScanOptionSet(Configuration settings) {
	return !settings.hasKey(assemblyPropertyKey) && 
		!settings.hasKey(projectFilePropertyKey)&& 
		!hasSlnFile(settings);
}

private boolean hasSlnFile(Configuration settings){
	return settings.hasKey(slnFilePropertyKey) || (this.alternativeSlnFile != null && this.alternativeSlnFile.length() > 0);
}
  
  private void checkMandatoryProperties(Configuration settings) {
	 
	    if (isNoScanOptionSet(settings)) {
	      throw new IllegalArgumentException("No FxCop analysis has been performed on this project, whereas it contains " + languageKey() + " files: " +
	        "Verify that you are using the latest version of the SonarQube Scanner for MSBuild, and if you do, please report a bug. " +
	        "In the short term, you can disable all FxCop rules from your quality profile to get rid of this error.");
	    }
	  }

  private void checkAssemblyProperty(Configuration settings) {
    Optional<String> assemblyPath = settings.get(assemblyPropertyKey);
    
    
    if (!assemblyPath.isPresent()) {
    	LOG.error("The property '" + assemblyPropertyKey + "' is not set.");
    	throw new IllegalArgumentException("The property '" + assemblyPropertyKey + "' is not set.");
    } else if (assemblyPath.get().contains("*")) {
    	checkWildcardAssemblyPath(assemblyPath.get());
    } else {
        checkSingleAssemblyPath(assemblyPath.get());
    }
  }

  private void checkWildcardAssemblyPath(String assemblyPath) {
	  int lastSlash = assemblyPath.lastIndexOf('/');
	  if (lastSlash <= 0) lastSlash = assemblyPath.lastIndexOf('\\');
	  String folderPath = lastSlash > 0 ?  assemblyPath.substring(0, lastSlash) : "./";
	  String fileName = lastSlash > 0 ?  assemblyPath.substring(lastSlash+1) : assemblyPath;
		
	  assemblyCount = 0;
	  countMatchingAssemblyFiles(assemblyPath, folderPath, fileName);
	  Preconditions.checkArgument(
  		      assemblyCount>0,
  		      "Cannot find any assembly matching \"" + fileName + "\" in folder \""+folderPath+PROVIDED_BY_THE_PROPERTY + assemblyPropertyKey + "\".");
  }

  private void countMatchingAssemblyFiles(String assemblyPath, String folderPath, String fileName) {
	try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(
	    Paths.get(folderPath), fileName)) {			
	    dirStream.forEach(path -> {
	      if (path.toString().endsWith(".dll") || path.toString().endsWith(".exe")) {
	    	LOG.debug("Check assembly: '" + path + "'.");
	    	File pdbFile = new File(pdbPath(path.toString()));
	    	if (pdbFile.isFile()) {
	           assemblyCount++;
	    	}  else {
	    		LOG.debug("Ignore file: '" + path + "' no pdb.");
	    	}
	      } else {
	    	  LOG.debug("Ignore file: '" + path + "' no assembly.");
	      }
	    });
	  } catch (IOException e) {
		  LOG.error("Error during search assemblys", e);
	    Preconditions.checkArgument(
	    		      false,
	    		      "Cannot find any assembly matching \"" + assemblyPath + PROVIDED_BY_THE_PROPERTY + assemblyPropertyKey + "\". Error: " + e.getMessage());
	  }
  }
  
  private void checkSingleAssemblyPath(String assemblyPath) {
	File assemblyFile = new File(assemblyPath);
    Preconditions.checkArgument(
      assemblyFile.isFile(),
      "Cannot find the assembly \"" + assemblyFile.getAbsolutePath() + PROVIDED_BY_THE_PROPERTY + assemblyPropertyKey + "\".");

    File pdbFile = new File(pdbPath(assemblyPath));
    Preconditions.checkArgument(
      pdbFile.isFile(),
      "Cannot find the .pdb file \"" + pdbFile.getAbsolutePath() + "\" inferred from the property \"" + assemblyPropertyKey + "\".");

  }

  private static String pdbPath(String assemblyPath) {
    int i = assemblyPath.lastIndexOf('.');
    if (i == -1) {
      i = assemblyPath.length();
    }

    return assemblyPath.substring(0, i) + ".pdb";
  }

  private void checkFxCopCmdPathProperty(Configuration settings) {
    if (!settings.hasKey(fxCopCmdPropertyKey) && settings.hasKey(DEPRECATED_FXCOPCMD_PATH_PROPERTY_KEY)) {
      fxCopCmdPropertyKey = DEPRECATED_FXCOPCMD_PATH_PROPERTY_KEY;
    }

    Optional<String> value = settings.get(fxCopCmdPropertyKey);
    
    Preconditions.checkArgument(
    		value.isPresent(),
    		"FxCopCmd executable is not set \"" + PROVIDED_BY_THE_PROPERTY + fxCopCmdPropertyKey + "\".");

        
    File file = new File(value.get());
    Preconditions.checkArgument(
      file.isFile(),
      "Cannot find the FxCopCmd executable \"" + file.getAbsolutePath() + PROVIDED_BY_THE_PROPERTY + fxCopCmdPropertyKey + "\".");
  }

  private void checkTimeoutProeprty(Configuration settings) {
    if (!settings.hasKey(timeoutPropertyKey) && settings.hasKey(DEPRECATED_TIMEOUT_MINUTES_PROPERTY_KEY)) {
      timeoutPropertyKey = DEPRECATED_TIMEOUT_MINUTES_PROPERTY_KEY;
    }
  }

  private void checkReportPathProperty(Configuration settings) {
	  
	  Optional<String> settingOptional = settings.get(reportPathPropertyKey);
	  
	  Preconditions.checkArgument(
			  settingOptional.isPresent(),
	      "The FxCop report  \"" + PROVIDED_BY_THE_PROPERTY + reportPathPropertyKey + IS_NOT_PRESENT);
	  
    File file = new File(settingOptional.get());
    Preconditions.checkArgument(
      file.isFile(),
      "Cannot find the FxCop report \"" + file.getAbsolutePath() + PROVIDED_BY_THE_PROPERTY + reportPathPropertyKey + "\".");
  }
  
  private void checkProjectFileProperty(Configuration settings) {
		if (!settings.hasKey(projectFilePropertyKey)){
			return;
		}
	    Optional<String> projectFilePath = settings.get(projectFilePropertyKey);
	    
	    Preconditions.checkArgument(
	    		projectFilePath.isPresent(),
	      "The project file \"" + PROVIDED_BY_THE_PROPERTY + projectFilePropertyKey + IS_NOT_PRESENT);

	    File assemblyFile = new File(projectFilePath.get());
	    Preconditions.checkArgument(
	      assemblyFile.isFile(),
	      "Cannot find the project \"" + assemblyFile.getAbsolutePath() + PROVIDED_BY_THE_PROPERTY + projectFilePropertyKey + "\".");

	    
	  }
  
  private void checkSlnProperty(Configuration settings) {
	    Optional<String> slnFilePath = settings.get(slnFilePropertyKey);
	    
	    if (!slnFilePath.isPresent()) slnFilePath = Optional.of(this.alternativeSlnFile);
	    
	    Preconditions.checkArgument(
	    		slnFilePath.isPresent(),
	      "The sln file \"" + PROVIDED_BY_THE_PROPERTY + slnFilePropertyKey + IS_NOT_PRESENT);


	    File slnFile = new File(slnFilePath.get());
	    Preconditions.checkArgument(
	    		slnFile.isFile(),
	      "Cannot find the sln file \"" + slnFile.getAbsolutePath() + PROVIDED_BY_THE_PROPERTY + slnFilePropertyKey + "\".");

	    
	  }

  public void setAlternativeSln(String altSlnFile) {
	this.alternativeSlnFile = altSlnFile;
	
  }

}
