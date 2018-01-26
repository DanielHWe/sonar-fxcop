package org.sonar.plugins.fxcop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.xml.sax.*;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.w3c.dom.*;

public class FxCopProjectGenerator {
	private static final String FX_COP_PLUGIN_FAILED_TO_SCAN_DUE_TO_FXCOP_CONFIGURATION_ISSUE = "FxCop Plugin failed to scan, due to fxcop configuration issue.";
	private static final Logger LOG = Loggers.get(FxCopSensor.class);

	 public String generate(String slnFileName) {
		LOG.info("FxCop start create FxCop configuration for '"+slnFileName+"'");
		File slnFileObj = new File(slnFileName);
		File fxCopConfigObj = createConfigFileObj(slnFileObj);
		
		String[] targetDlls = getTargetFiles(slnFileObj);
		
		Document dom = createConfigObject(targetDlls);		
		saveToXML(dom, fxCopConfigObj);
		LOG.debug("Finish create FxCop configuration '"+fxCopConfigObj.getAbsolutePath()+"'");
		return fxCopConfigObj.getAbsolutePath();
	 }
	 
	 private String[] getTargetFiles(File slnFileObj) {
		 try {
			String[] csprojFiles = getCsprojForSolution(slnFileObj);
			List<String> targetFileList = new ArrayList<>();
			for (String project : csprojFiles) {
				addProjectAssemblyIfExists(targetFileList, project);
			}
			
			if (targetFileList.isEmpty()) {
				LOG.error("No projects found to scan, can not generate FxCop configuration.");
				throw new IllegalArgumentException("No projects found to scan, can not generate FxCop configuration.");
			}
			
			return targetFileList.toArray(new String[targetFileList.size()]);
		 } catch (IOException ex){
			 LOG.error("Fail to read fxcop '"+slnFileObj.getName()+"': " + ex.getMessage());
	         throw new IllegalStateException("FxCop Plugin failed to scan, sln or csproj file was invalid.", ex);
		 }
	}

	private void addProjectAssemblyIfExists(List<String> targetFileList, String project) throws IOException {
		try {
			targetFileList.add(getDllPathFromCsProj(project));
		} catch (IllegalArgumentException iae){
			LOG.warn("Ignore '"+project+"' due to parsing error.");
		}
	}

	public String getDllPathFromCsProj(String project) throws IOException {
		LOG.debug("Add '"+project+"' to FxCop configuration.");
		
		
		
		//Pattern.matches("<OutputType>(\\w+)</OutputType>", input)
		   
		CSharpProjectInfo projectInfo = new CSharpProjectInfo(project);
		return projectInfo.getDllPathFromExistingBinary();
	    
	}

	

	public String[] getCsprojForSolution(File slnFileObj) throws IOException {
		List<String> result = new ArrayList<>();
		final Pattern pattern = Pattern.compile("\\\"([\\w\\.\\\\\\ \\-]+\\.csproj)\\\"");
		   
		String parentDir = slnFileObj.getParent();
		if (parentDir == null) {
			parentDir = "";
		}
		
       try (final BufferedReader reader = new BufferedReader(new FileReader(slnFileObj))) {
	       while(reader.ready()) {
	    	   Matcher m = pattern.matcher(reader.readLine());
	           if (m.find()) {        	   
	        	   String file = Paths.get(parentDir, m.group(1)).toString();
	        	   if (!(new File(file).exists())){	        		   
	        		   LOG.error("Project File not '"+file+"' found for " + slnFileObj.getAbsolutePath());
	        		   throw new IllegalStateException("Project File not found: " + file);
	        	   }
	        	   result.add(file);
	           }
	       }
       }
       
       return result.toArray(new String[result.size()]);
	}

	private Document createConfigObject(String[] targetDlls) {
		 Document dom;
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(XML_TEMPLATE));
            dom = db.parse(is);

            Element doc = dom.getDocumentElement();
            NodeList targetsNodeList = doc.getElementsByTagName("Targets");
            if (targetsNodeList.getLength() < 1) throw new IllegalStateException("Targets not found in project temnplate.");
            Node targetsNode = targetsNodeList.item(0);
            
            for (String targetFileName : targetDlls) {
				Element targetNode = dom.createElement("Target");
				targetNode.setAttribute("Name", targetFileName);
				targetNode.setAttribute("Analyze", "True");
				targetNode.setAttribute("AnalyzeAllChildren", "True");
				targetsNode.appendChild(targetNode);
			}
         
        } catch (Exception ex) {
        	LOG.error("Fail to read fxcop configuration template: " + ex.getMessage());
            throw new IllegalStateException(FX_COP_PLUGIN_FAILED_TO_SCAN_DUE_TO_FXCOP_CONFIGURATION_ISSUE, ex);
        } 
		return dom;
	}

	private void saveToXML(Document dom, File fxCopConfigObj) {

	    try (FileOutputStream fs = new FileOutputStream(fxCopConfigObj.getAbsoluteFile())) {
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty(OutputKeys.METHOD, "xml");
            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            
            // send DOM to file
            tr.transform(new DOMSource(dom), 
                                 new StreamResult(fs));

        } catch (TransformerException te) {
        	LOG.error("Fail to create fxcop configuration: " + te.getMessage());
            throw new IllegalStateException(FX_COP_PLUGIN_FAILED_TO_SCAN_DUE_TO_FXCOP_CONFIGURATION_ISSUE, te);
        } catch (IOException ioe) {
        	LOG.error("Fail to write fxcop configuration: " + ioe.getMessage());
        	throw new IllegalStateException(FX_COP_PLUGIN_FAILED_TO_SCAN_DUE_TO_FXCOP_CONFIGURATION_ISSUE, ioe);
        }
	    
	}

	private File createConfigFileObj(File slnFileObj) {
		 Date dNow = new Date( );
	     SimpleDateFormat ft = 
	      new SimpleDateFormat (".yyyyMMddhhmmss");
	     
		return new File(slnFileObj.getAbsoluteFile() + ft.format(dNow) + ".fxcop");
	}
	
	private static final String XML_TEMPLATE =
			"<FxCopProject Version=\"1.36\" Name=\"My FxCop Project\">" +
		" <ProjectOptions>" +
		"  <SharedProject>True</SharedProject>" +
		"  <Stylesheet Apply=\"False\">c:\\program files (x86)\\microsoft fxcop 1.36\\Xml\\FxCopReport.xsl</Stylesheet>" +
		"  <SaveMessages>" +
		"   <Project Status=\"Active, Excluded\" NewOnly=\"False\" />" +
		"   <Report Status=\"Active\" NewOnly=\"False\" />" +
		"  </SaveMessages>" +
		"  <ProjectFile Compress=\"True\" DefaultTargetCheck=\"True\" DefaultRuleCheck=\"True\" SaveByRuleGroup=\"\" Deterministic=\"True\" />" +
		"  <EnableMultithreadedLoad>True</EnableMultithreadedLoad>" +
		"  <EnableMultithreadedAnalysis>True</EnableMultithreadedAnalysis>" +
		"  <SourceLookup>True</SourceLookup>" +
		"  <AnalysisExceptionsThreshold>10</AnalysisExceptionsThreshold>" +
		"  <RuleExceptionsThreshold>1</RuleExceptionsThreshold>" +
		"  <Spelling Locale=\"de-DE\" />" +
		"  <OverrideRuleVisibilities>False</OverrideRuleVisibilities>" +
		"  <CustomDictionaries SearchFxCopDir=\"True\" SearchUserProfile=\"True\" SearchProjectDir=\"True\" />" +
		"  <SearchGlobalAssemblyCache>False</SearchGlobalAssemblyCache>" +
		"  <DeadlockDetectionTimeout>120</DeadlockDetectionTimeout>" +
		"  <IgnoreGeneratedCode>False</IgnoreGeneratedCode>" +
		" </ProjectOptions>" +
		" <Targets>" +
		//"  <Target Name=\"$(ProjectDir)/../projects/Components/Mail/Dev/MailSample/bin/Debug/Haufe.Components.Mail.dll\" Analyze=\"True\" AnalyzeAllChildren=\"True\" />" +
		//"  <Target Name=\"$(ProjectDir)/../projects/Components/Mail/Dev/MailSample/bin/Debug/Haufe.Components.MailSample.exe\" Analyze=\"True\" AnalyzeAllChildren=\"True\" />" +
		" </Targets>" +
		" <Rules>" +
		"  <RuleFiles>" +
		"   <RuleFile Name=\"$(FxCopDir)\\Rules\\DesignRules.dll\" Enabled=\"True\" AllRulesEnabled=\"True\" />" +
		"   <RuleFile Name=\"$(FxCopDir)\\Rules\\GlobalizationRules.dll\" Enabled=\"True\" AllRulesEnabled=\"True\" />" +
		"   <RuleFile Name=\"$(FxCopDir)\\Rules\\InteroperabilityRules.dll\" Enabled=\"True\" AllRulesEnabled=\"True\" />" +
		"   <RuleFile Name=\"$(FxCopDir)\\Rules\\MobilityRules.dll\" Enabled=\"True\" AllRulesEnabled=\"True\" />" +
		"   <RuleFile Name=\"$(FxCopDir)\\Rules\\NamingRules.dll\" Enabled=\"True\" AllRulesEnabled=\"True\" />" +
		"   <RuleFile Name=\"$(FxCopDir)\\Rules\\PerformanceRules.dll\" Enabled=\"True\" AllRulesEnabled=\"True\" />" +
		"   <RuleFile Name=\"$(FxCopDir)\\Rules\\PortabilityRules.dll\" Enabled=\"True\" AllRulesEnabled=\"True\" />" +
		"   <RuleFile Name=\"$(FxCopDir)\\Rules\\SecurityRules.dll\" Enabled=\"True\" AllRulesEnabled=\"True\" />" +
		"   <RuleFile Name=\"$(FxCopDir)\\Rules\\UsageRules.dll\" Enabled=\"True\" AllRulesEnabled=\"True\" />" +
		"  </RuleFiles>" +
		"  <Groups />" +
		"  <Settings />" +
		" </Rules>" +
		" <FxCopReport Version=\"1.36\" />" +
		"</FxCopProject>";
}
