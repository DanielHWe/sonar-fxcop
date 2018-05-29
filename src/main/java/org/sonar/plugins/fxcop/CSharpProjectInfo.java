package org.sonar.plugins.fxcop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class CSharpProjectInfo {
	private static final Logger LOG = Loggers.get(FxCopSensor.class);
	static final Pattern patternType = Pattern.compile("<OutputType>([\\w]+)</OutputType>");
	static final Pattern patternName = Pattern.compile("<AssemblyName>([\\w\\-\\ \\.]+)</AssemblyName>");
	static final Pattern patternPath = Pattern.compile("<OutputPath>([\\w\\-\\ \\.\\\\]+)</OutputPath>");
	static final Pattern patternTargetFramework = Pattern.compile("<TargetFramework>([\\w\\-\\ \\.\\\\]+)</TargetFramework>");
	
	private String project = null;
	private String name = null;
	private String type = null;
	private String targetFramework;
	private List<String> paths = new ArrayList<>();
	
	CSharpProjectInfo(String project) throws IOException{
		this.project = project;
		scanProjectFile();
		performOnNetCore();
		checkAllRequiredValuesFound();
	}

	private void performOnNetCore() {
		//Net core project files contain only non default settings, so set defaults if not set
		if (targetFramework!=null && targetFramework.startsWith("netcoreapp")){
			if (paths.isEmpty()) {
				paths.add(convertPath("bin\\Debug\\netcoreapp2.0"));
				paths.add(convertPath("bin\\Release\\netcoreapp2.0"));
			}
			if (type == null){
				type = "Library";
			}
			if (name == null) {
				File projectFile = new File(project);
				name = projectFile.getName().replace(".csproj", "");
			}
		}
		
	}

	private void checkAllRequiredValuesFound() {
		if (paths == null || paths.isEmpty()) {
	    	LOG.warn("No output path found for '"+project+"'.");
	    	throw new IllegalArgumentException("No output path found for '"+project+"'.");
	    }
	    if (type == null) {
	    	LOG.warn("No output type found for '"+project+"'.");
	    	throw new IllegalArgumentException("No output type found for '"+project+"'.");
	    }
	    if (name == null || name.isEmpty()) {
	    	LOG.warn("No output name found for '"+project+"'.");
	    	throw new IllegalArgumentException("No output name found for '"+project+"'.");
	    }
		
	}

	private void scanProjectFile() throws IOException {
		final BufferedReader reader = new BufferedReader(new FileReader(new File(project)));
		try {
	       while(reader.ready()) {
	    	   String currentLine = reader.readLine();
	    	   Matcher m = patternType.matcher(currentLine);
	           if (m.find()) {
	        	   type = (m.group(1));
	           }
	           m = patternName.matcher(currentLine);
	           if (m.find()) {
	        	   name = (m.group(1));
	           }
	           m = patternPath.matcher(currentLine);
	           if (m.find()) {
	        	   paths.add(convertPath(m.group(1)));	        	   
	           }
	           m = patternTargetFramework.matcher(currentLine);
	           if (m.find()) {
	        	   targetFramework = m.group(1);	        	   
	           }
	       }
		} finally {
	       reader.close();
		}
	}
	
	private String convertPath(String path){
		if (File.pathSeparator.equals("\\")) {
 		   return path;
 	   }
		return path.replace('\\', '/');
	}

	public String getDllPathFromExistingBinary() {
		File projectFile = new File(project);
		String binFileName = getBinFileName(type, name);
	    Path result = null;
	    String parentDir = projectFile.getParent();
	    if (parentDir==null) parentDir = ".";
	    StringBuilder sbPath = new StringBuilder();
	    
	    for (String path : paths) {
			try {
				result = Paths.get(parentDir, path, binFileName).toRealPath();
				break;
			} catch (IOException ex){
				if (sbPath.length()>0) sbPath.append(", ");
				sbPath.append(Paths.get(parentDir, path).toString());
				LOG.info(ex.getMessage());
				result = null;
			}
		}
	    if (result == null) {
			LOG.error(binFileName + " was not found in any output directory ("+sbPath+"), please build project before scan.");
			throw new IllegalStateException(binFileName + " was not found in any output directory ("+sbPath+"), please build project before scan.");
	    }
		return result.toString();
	}
	
	private String getBinFileName(String type, String name) {
		if (type.equalsIgnoreCase("Library")){
			return name + ".dll";
		}
		return name + ".exe";
	}
}
