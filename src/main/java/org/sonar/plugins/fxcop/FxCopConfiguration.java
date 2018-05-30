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

import org.sonar.api.config.Settings;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class FxCopConfiguration {

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

  public boolean checkProperties(Settings settings) {
	    if (settings.hasKey(reportPathPropertyKey)) {
	      checkReportPathProperty(settings);
	    } else {
	    	if (!settings.hasKey(assemblyPropertyKey) && 
	    		!settings.hasKey(projectFilePropertyKey)&& 
	    		!settings.hasKey(slnFilePropertyKey)){
	    		LOG.warn(MISSING_SCAN_DEFINITION_TEXT);
	    		if (this.alternativeSlnFile != null && !this.alternativeSlnFile.isEmpty()) {
	    			LOG.warn("Use default sln file found: " + this.alternativeSlnFile);
	    			settings.appendProperty(slnFilePropertyKey, this.alternativeSlnFile);
	    		} else {
	    			LOG.error("No possible default found sln, please specify.");
	    			throw new IllegalArgumentException(MISSING_SCAN_DEFINITION_TEXT);
	    		}
	    	}
	    	
	    	if (!settings.hasKey(assemblyPropertyKey) && 
	    		settings.hasKey(projectFilePropertyKey)&& 
	    		!settings.hasKey(slnFilePropertyKey)) 
	    	{ 
	    		checkMandatoryProperties(settings); 
	    		checkProjectFileProperty(settings);
		        checkFxCopCmdPathProperty(settings);
		        checkTimeoutProeprty(settings);
	    	} 
	    	else if (!settings.hasKey(assemblyPropertyKey) && 
		    		!settings.hasKey(projectFilePropertyKey)&& 
		    		settings.hasKey(slnFilePropertyKey)) 
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
	    return true;
	  }
  
  private void checkMandatoryProperties(Settings settings) {
	 
	    if (!settings.hasKey(assemblyPropertyKey) && !settings.hasKey(projectFilePropertyKey) && !settings.hasKey(slnFilePropertyKey)) {
	      throw new IllegalArgumentException("No FxCop analysis has been performed on this project, whereas it contains " + languageKey() + " files: " +
	        "Verify that you are using the latest version of the SonarQube Scanner for MSBuild, and if you do, please report a bug. " +
	        "In the short term, you can disable all FxCop rules from your quality profile to get rid of this error.");
	    }
	  }

  private void checkAssemblyProperty(Settings settings) {
    String assemblyPath = settings.getString(assemblyPropertyKey);

    if (assemblyPath == null) {
    	LOG.error("The property '" + assemblyPropertyKey + "' is not set.");
    	throw new IllegalArgumentException("The property '" + assemblyPropertyKey + "' is not set.");
    } else if (assemblyPath.contains("*")) {
    	checkWildcardAssemblyPath(assemblyPath);
    } else {
        checkSingleAssemblyPath(assemblyPath);
    }
  }

  private void checkWildcardAssemblyPath(String assemblyPath) {
	  int lastSlash = assemblyPath.lastIndexOf('/');
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

  private void checkFxCopCmdPathProperty(Settings settings) {
    if (!settings.hasKey(fxCopCmdPropertyKey) && settings.hasKey(DEPRECATED_FXCOPCMD_PATH_PROPERTY_KEY)) {
      fxCopCmdPropertyKey = DEPRECATED_FXCOPCMD_PATH_PROPERTY_KEY;
    }

    String value = settings.getString(fxCopCmdPropertyKey);

    Preconditions.checkArgument(
    		value != null,
    	      "FxCopCmd executable is not set \"" + PROVIDED_BY_THE_PROPERTY + fxCopCmdPropertyKey + "\".");
    
    File file = new File(value);
    Preconditions.checkArgument(
      file.isFile(),
      "Cannot find the FxCopCmd executable \"" + file.getAbsolutePath() + PROVIDED_BY_THE_PROPERTY + fxCopCmdPropertyKey + "\".");
  }

  private void checkTimeoutProeprty(Settings settings) {
    if (!settings.hasKey(timeoutPropertyKey) && settings.hasKey(DEPRECATED_TIMEOUT_MINUTES_PROPERTY_KEY)) {
      timeoutPropertyKey = DEPRECATED_TIMEOUT_MINUTES_PROPERTY_KEY;
    }
  }

  private void checkReportPathProperty(Settings settings) {
    File file = new File(settings.getString(reportPathPropertyKey));
    Preconditions.checkArgument(
      file.isFile(),
      "Cannot find the FxCop report \"" + file.getAbsolutePath() + PROVIDED_BY_THE_PROPERTY + reportPathPropertyKey + "\".");
  }
  
  private void checkProjectFileProperty(Settings settings) {
		if (!settings.hasKey(projectFilePropertyKey)){
			return;
		}
	    String projectFilePath = settings.getString(projectFilePropertyKey);

	    File assemblyFile = new File(projectFilePath);
	    Preconditions.checkArgument(
	      assemblyFile.isFile(),
	      "Cannot find the project \"" + assemblyFile.getAbsolutePath() + PROVIDED_BY_THE_PROPERTY + projectFilePropertyKey + "\".");

	    
	  }
  
  private void checkSlnProperty(Settings settings) {
		if (!settings.hasKey(slnFilePropertyKey)){
			return;
		}
	    String slnFilePath = settings.getString(slnFilePropertyKey);

	    File slnFile = new File(slnFilePath);
	    Preconditions.checkArgument(
	    		slnFile.isFile(),
	      "Cannot find the sln file \"" + slnFile.getAbsolutePath() + PROVIDED_BY_THE_PROPERTY + slnFilePropertyKey + "\".");

	    
	  }

  public void setAlternativeSln(String altSlnFile) {
	this.alternativeSlnFile = altSlnFile;
	
  }

}
