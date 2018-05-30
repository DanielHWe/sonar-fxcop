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

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.config.Settings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FxCopConfigurationTest {

  private static final String CONFIG_FILE_PATH = new File("src/test/resources/FxCopConfigurationTest/fxcop-report.xml").getAbsolutePath();
@Rule
  public ExpectedException thrown = ExpectedException.none();

	@BeforeClass 
	public static void onlyOnce() throws IOException {
		File f = new File(CONFIG_FILE_PATH);
		f.createNewFile();
 }

	@AfterClass
	public static void onlyOnceCleanup() throws IOException {
		File f = new File(CONFIG_FILE_PATH);
		f.delete();
 }
	
  @Test
  public void test() {
    FxCopConfiguration fxCopConf = new FxCopConfiguration("cs", "cs-fxcop", "fooAssemblyKey", "", "", "fooFxCopCmdPathKey", "fooTimeoutKey", "fooAspnetKey", "fooDirectoriesKey",
      "fooReferencesKey", "fooReportPathKey");
    assertThat(fxCopConf.languageKey()).isEqualTo("cs");
    assertThat(fxCopConf.repositoryKey()).isEqualTo("cs-fxcop");
    assertThat(fxCopConf.assemblyPropertyKey()).isEqualTo("fooAssemblyKey");
    assertThat(fxCopConf.fxCopCmdPropertyKey()).isEqualTo("fooFxCopCmdPathKey");
    assertThat(fxCopConf.timeoutPropertyKey()).isEqualTo("fooTimeoutKey");
    assertThat(fxCopConf.aspnetPropertyKey()).isEqualTo("fooAspnetKey");
    assertThat(fxCopConf.directoriesPropertyKey()).isEqualTo("fooDirectoriesKey");
    assertThat(fxCopConf.referencesPropertyKey()).isEqualTo("fooReferencesKey");
    assertThat(fxCopConf.reportPathPropertyKey()).isEqualTo("fooReportPathKey");

    fxCopConf = new FxCopConfiguration("vbnet", "vbnet-fxcop", "barAssemblyKey", "", "", "barFxCopCmdPathKey", "barTimeoutKey", "barAspnetKey", "barDirectoriesKey",
      "barReferencesKey", "barReportPathKey");
    assertThat(fxCopConf.languageKey()).isEqualTo("vbnet");
    assertThat(fxCopConf.repositoryKey()).isEqualTo("vbnet-fxcop");
    assertThat(fxCopConf.assemblyPropertyKey()).isEqualTo("barAssemblyKey");
    assertThat(fxCopConf.fxCopCmdPropertyKey()).isEqualTo("barFxCopCmdPathKey");
    assertThat(fxCopConf.timeoutPropertyKey()).isEqualTo("barTimeoutKey");
    assertThat(fxCopConf.aspnetPropertyKey()).isEqualTo("barAspnetKey");
    assertThat(fxCopConf.directoriesPropertyKey()).isEqualTo("barDirectoriesKey");
    assertThat(fxCopConf.referencesPropertyKey()).isEqualTo("barReferencesKey");
    assertThat(fxCopConf.reportPathPropertyKey()).isEqualTo("barReportPathKey");
  }
  
  @Test
  public void testWildCard() {
    FxCopConfiguration fxCopConf = new FxCopConfiguration("cs", "cs-fxcop", "foo/AssemblyKey*", "", "", "fooFxCopCmdPathKey", "fooTimeoutKey", "fooAspnetKey", "fooDirectoriesKey",
      "fooReferencesKey", "fooReportPathKey");
    assertThat(fxCopConf.languageKey()).isEqualTo("cs");
    assertThat(fxCopConf.repositoryKey()).isEqualTo("cs-fxcop");
    assertThat(fxCopConf.assemblyPropertyKey()).isEqualTo("foo/AssemblyKey*");
    assertThat(fxCopConf.fxCopCmdPropertyKey()).isEqualTo("fooFxCopCmdPathKey");
    assertThat(fxCopConf.timeoutPropertyKey()).isEqualTo("fooTimeoutKey");
    assertThat(fxCopConf.aspnetPropertyKey()).isEqualTo("fooAspnetKey");
    assertThat(fxCopConf.directoriesPropertyKey()).isEqualTo("fooDirectoriesKey");
    assertThat(fxCopConf.referencesPropertyKey()).isEqualTo("fooReferencesKey");
    assertThat(fxCopConf.reportPathPropertyKey()).isEqualTo("fooReportPathKey");

    fxCopConf = new FxCopConfiguration("vbnet", "vbnet-fxcop", "bar/AssemblyKey*", "", "", "barFxCopCmdPathKey", "barTimeoutKey", "barAspnetKey", "barDirectoriesKey",
      "barReferencesKey", "barReportPathKey");
    assertThat(fxCopConf.languageKey()).isEqualTo("vbnet");
    assertThat(fxCopConf.repositoryKey()).isEqualTo("vbnet-fxcop");
    assertThat(fxCopConf.assemblyPropertyKey()).isEqualTo("bar/AssemblyKey*");
    assertThat(fxCopConf.fxCopCmdPropertyKey()).isEqualTo("barFxCopCmdPathKey");
    assertThat(fxCopConf.timeoutPropertyKey()).isEqualTo("barTimeoutKey");
    assertThat(fxCopConf.aspnetPropertyKey()).isEqualTo("barAspnetKey");
    assertThat(fxCopConf.directoriesPropertyKey()).isEqualTo("barDirectoriesKey");
    assertThat(fxCopConf.referencesPropertyKey()).isEqualTo("barReferencesKey");
    assertThat(fxCopConf.reportPathPropertyKey()).isEqualTo("barReportPathKey");
  }

  @Test
  public void check_properties() {
    Settings settings = mock(Settings.class);
    when(settings.hasKey("fooAssemblyKey")).thenReturn(true);
    when(settings.getString("fooAssemblyKey")).thenReturn(new File("src/test/resources/FxCopConfigurationTest/MyLibrary.dll").getAbsolutePath());
    when(settings.hasKey("fooFxCopCmdPathKey")).thenReturn(true);
    when(settings.getString("fooFxCopCmdPathKey")).thenReturn(new File("src/test/resources/FxCopConfigurationTest/FxCopCmd.exe").getAbsolutePath());
    when(settings.hasKey("fooFxCopReportPathKey")).thenReturn(true);
    when(settings.getString("fooFxCopReportPathKey")).thenReturn(CONFIG_FILE_PATH);

    FxCopConfiguration config = new FxCopConfiguration("", "", "fooAssemblyKey", "", "", "fooFxCopCmdPathKey", "", "", "", "", "fooFxCopReportPathKey");
    
    config.checkProperties(settings);
    config.setAlternativeSln("abc");
  }

  @Test
  public void check_properties_without_assembly_extension() {
    Settings settings = mock(Settings.class);
    when(settings.hasKey("fooAssemblyKey")).thenReturn(true);
    when(settings.getString("fooAssemblyKey")).thenReturn(new File("src/test/resources/FxCopConfigurationTest/MyLibrary").getAbsolutePath());
    when(settings.hasKey("fooFxCopCmdPathKey")).thenReturn(true);
    when(settings.getString("fooFxCopCmdPathKey")).thenReturn(new File("src/test/resources/FxCopConfigurationTest/FxCopCmd.exe").getAbsolutePath());

    new FxCopConfiguration("", "", "fooAssemblyKey", "", "", "fooFxCopCmdPathKey", "", "", "", "", "").checkProperties(settings);
  }
  
  @Test
  public void check_properties_with_project_property_is_set() {
	  thrown.expect(IllegalArgumentException.class);
	    thrown.expectMessage("Cannot find the project");
    Settings settings = mock(Settings.class);
    String projectProperty = "fooAssemblyKey";
    settings.setProperty("projectFileProperty", "fooAssemblyKey");
    when(settings.hasKey(projectProperty)).thenReturn(true);
    when(settings.getString(projectProperty)).thenReturn("src/test/resources/FxCopConfigurationTest/abc.fxCopProject");
    when(settings.hasKey("fooFxCopCmdPathKey")).thenReturn(true);
    when(settings.getString("fooFxCopCmdPathKey")).thenReturn(new File("src/test/resources/FxCopConfigurationTest/FxCopCmd.exe").getAbsolutePath());


    FxCopConfiguration config = new FxCopConfiguration("", "", null, projectProperty, "", "", "", "", "", "", "");
    assertThat(config.checkProperties(settings)).isFalse();
  }



  @Test(expected = IllegalArgumentException.class)
  public void check_properties_should_return_false_when_assembly_property_not_found() {

    Settings settings = mock(Settings.class);

    String assemblyProperty = "fooAssemblyKey";

    when(settings.hasKey(assemblyProperty)).thenReturn(false);



    FxCopConfiguration config = new FxCopConfiguration("", "", assemblyProperty, "", "", "", "", "", "", "", "");

    assertThat(config.checkProperties(settings)).isFalse();
  }

  @Test(expected = IllegalArgumentException.class)
  public void check_properties_should_return_true_when_assembly_property_is_set() {
    Settings settings = mock(Settings.class);
    String assemblyProperty = "fooAssemblyKey";
    settings.setProperty("assemblyProperty", assemblyProperty);
    when(settings.hasKey(assemblyProperty)).thenReturn(false);

    FxCopConfiguration config = new FxCopConfiguration("", "", assemblyProperty, "", null, "", "", "", "", "", "");
    assertThat(config.checkProperties(settings)).isFalse();
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void check_properties_should_return_true_when_project_property_is_set() {
    Settings settings = mock(Settings.class);
    String projectProperty = "fooAssemblyKey";
    settings.setProperty("projectFileProperty", "fooAssemblyKey");
    when(settings.hasKey(projectProperty)).thenReturn(false);

    FxCopConfiguration config = new FxCopConfiguration("", "", null, projectProperty, "", "", "", "", "", "", "");
    assertThat(config.checkProperties(settings)).isFalse();
  }
  
  
  
  @Test
  public void check_properties_assembly_property_pdb_not_found() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Cannot find the .pdb file");
    thrown.expectMessage(new File("src/test/resources/FxCopConfigurationTest/MyLibraryWithoutPdb.pdb").getAbsolutePath());
    thrown.expectMessage("\"fooAssemblyKey\"");

    Settings settings = mock(Settings.class);
    when(settings.hasKey("fooAssemblyKey")).thenReturn(true);
    when(settings.getString("fooAssemblyKey")).thenReturn(new File("src/test/resources/FxCopConfigurationTest/MyLibraryWithoutPdb.dll").getAbsolutePath());

    new FxCopConfiguration("", "", "fooAssemblyKey", "", "", "", "", "", "", "", "").checkProperties(settings);
  }
  
  @Test
  public void check_properties_assembly_property_null() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("The property");
    thrown.expectMessage("is not set");
    thrown.expectMessage("\'fooAssemblyKey\'");

    Settings settings = mock(Settings.class);
    when(settings.hasKey("fooAssemblyKey")).thenReturn(true);
    when(settings.getString("fooAssemblyKey")).thenReturn(null);

    new FxCopConfiguration("", "", "fooAssemblyKey", "", "", "", "", "", "", "", "").checkProperties(settings);
  }

  @Test
  public void check_properties_fxcopcmd_property_deprecated() {
    Settings settings = new Settings();
    settings.setProperty("fooAssemblyKey", "src/test/resources/FxCopConfigurationTest/MyLibrary.dll");
    settings.setProperty("sonar.fxcop.installDirectory", new File("src/test/resources/FxCopConfigurationTest/FxCopCmd.exe").getAbsolutePath());

    FxCopConfiguration fxCopConf = new FxCopConfiguration("", "", "fooAssemblyKey", "", "", "fooFxCopCmdPathKey", "", "", "", "", "");
    fxCopConf.checkProperties(settings);

    assertThat(settings.getString(fxCopConf.fxCopCmdPropertyKey())).isEqualTo(new File("src/test/resources/FxCopConfigurationTest/FxCopCmd.exe").getAbsolutePath());
  }

  @Test
  public void check_properties_fxcopcmd_property_not_found() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Cannot find the FxCopCmd executable");
    thrown.expectMessage(new File("src/test/resources/FxCopConfigurationTest/FxCopCmdNotFound.exe").getAbsolutePath());
    thrown.expectMessage("\"fooFxCopCmdPathKey\"");

    Settings settings = new Settings();
    settings.setProperty("fooAssemblyKey", "src/test/resources/FxCopConfigurationTest/MyLibrary.dll");
    settings.setProperty("fooFxCopCmdPathKey", new File("src/test/resources/FxCopConfigurationTest/FxCopCmdNotFound.exe").getAbsolutePath());

    new FxCopConfiguration("", "", "fooAssemblyKey", "", "", "fooFxCopCmdPathKey", "", "", "", "", "").checkProperties(settings);
  }
  
  @Test
  public void check_properties_fxcopcmd_property_not_found_slnFile() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Cannot find the FxCopCmd executable");
    thrown.expectMessage(new File("src/test/resources/FxCopConfigurationTest/FxCopCmdNotFound.exe").getAbsolutePath());
    thrown.expectMessage("\"fooFxCopCmdPathKey\"");

    Settings settings = new Settings();
    settings.setProperty("fooSlnKey", "src/test/resources/FxCopConfigGeneratorTests/TestApp1.sln");
    settings.setProperty("fooFxCopCmdPathKey", new File("src/test/resources/FxCopConfigurationTest/FxCopCmdNotFound.exe").getAbsolutePath());

    new FxCopConfiguration("", "", "", "", "fooSlnKey", "fooFxCopCmdPathKey", "", "", "", "", "").checkProperties(settings);
  }

  @Test
  public void check_properties_timeout_property_deprecated() {
    Settings settings = new Settings();
    settings.setProperty("fooAssemblyKey", "src/test/resources/FxCopConfigurationTest/MyLibrary.dll");
    settings.setProperty("fooFxCopCmdPathKey", new File("src/test/resources/FxCopConfigurationTest/FxCopCmd.exe").getAbsolutePath());
    settings.setProperty("sonar.fxcop.timeoutMinutes", "42");

    FxCopConfiguration fxCopConf = new FxCopConfiguration("", "", "fooAssemblyKey", "", "", "fooFxCopCmdPathKey", "fooTimeoutKey", "", "", "", "");
    fxCopConf.checkProperties(settings);

    assertThat(settings.getString(fxCopConf.timeoutPropertyKey())).isEqualTo("42");
  }
  
  @Test
  public void check_properties_timeout_property_deprecated_sln() {
    Settings settings = new Settings();
    settings.setProperty("fooSlnKey", "src/test/resources/FxCopConfigGeneratorTests/TestApp1.sln");
    settings.setProperty("fooFxCopCmdPathKey", new File("src/test/resources/FxCopConfigurationTest/FxCopCmd.exe").getAbsolutePath());
    settings.setProperty("sonar.fxcop.timeoutMinutes", "42");

    FxCopConfiguration fxCopConf = new FxCopConfiguration("", "", "", "", "fooSlnKey", "fooFxCopCmdPathKey", "fooTimeoutKey", "", "", "", "");
    fxCopConf.checkProperties(settings);

    assertThat(settings.getString(fxCopConf.timeoutPropertyKey())).isEqualTo("42");
  }

  @Test
  public void check_properties_report_assembly_path_not_found() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Cannot find the assembly");    

    Settings settings = new Settings();
    settings.setProperty("fooAssemblyKey", new File("src/test/resources/FxCopConfigurationTest/fxcop-report-notfound.xml").getAbsolutePath());

    FxCopConfiguration fxCopConf = new FxCopConfiguration("", "", "fooAssemblyKey", "", "", "fooFxCopCmdPathKey", "", "", "", "", "");
    fxCopConf.checkProperties(settings);
  }
  
  @Test
  public void check_properties_assembly_wildcard_path_not_found() {
	    thrown.expect(IllegalArgumentException.class);
	    thrown.expectMessage("Cannot find any assembly matching");    

	    Settings settings = new Settings();
	    settings.setProperty("fooAssemblyKey", new File("src/test/resources/FxCopConfigurationTest/fxcop*").getAbsolutePath());

	    FxCopConfiguration fxCopConf = new FxCopConfiguration("", "", "fooAssemblyKey", "", "", "fooFxCopCmdPathKey", "", "", "", "", "");
	    fxCopConf.checkProperties(settings);
	  }
  
  @Test
  public void check_properties_report_project_path_not_found() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Cannot find the FxCop report");
    thrown.expectMessage(new File("src/test/resources/FxCopConfigurationTest/fxcop-report-notfound.xml").getAbsolutePath());
    thrown.expectMessage("\"fooReportPathKey\"");

    Settings settings = new Settings();
    settings.setProperty("fooReportPathKey", new File("src/test/resources/FxCopConfigurationTest/fxcop-report-notfound.xml").getAbsolutePath());

    FxCopConfiguration fxCopConf = new FxCopConfiguration("", "", "", "fooProjectPath", "", "fooFxCopCmdPathKey", "", "", "", "", "fooReportPathKey");
    fxCopConf.checkProperties(settings);
  }
  
  @Test
  public void check_properties_report_project() {
    
    Settings settings = new Settings();
    settings.setProperty("fooReportPathKey", CONFIG_FILE_PATH);

    FxCopConfiguration fxCopConf = new FxCopConfiguration("", "", "", "fooProjectPath", "", "fooFxCopCmdPathKey", "", "", "", "", "fooReportPathKey");
    fxCopConf.checkProperties(settings);
    
    assertThat(fxCopConf.projectFilePropertyKey()).isEqualTo("fooProjectPath");
  }
  
  @Test
  public void check_properties_sln_path() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Cannot find the sln file");   

    Settings settings = new Settings();
    settings.setProperty("slnFilePath", new File("src/test/resources/FxCopConfigurationTest/sln-notfound.sln").getAbsolutePath());

    FxCopConfiguration fxCopConf = new FxCopConfiguration("CS", "", "", "", "slnFilePath", "fooFxCopCmdPathKey", "", "", "", "", "");
    fxCopConf.checkProperties(settings);
  }
  
  @Test
  public void check_properties_sln_path_not_found() {   

    Settings settings = new Settings();
    settings.setProperty("fooFxCopCmdPathKey", new File("src/test/resources/FxCopConfigurationTest/FxCopCmd.exe").getAbsolutePath());    
    settings.setProperty("slnFilePath", new File("src/test/resources/FxCopConfigGeneratorTests/TestApp1.sln").getAbsolutePath());

    FxCopConfiguration fxCopConf = new FxCopConfiguration("CS", "", "", "", "slnFilePath", "fooFxCopCmdPathKey", "", "", "", "", "");
    
    fxCopConf.checkProperties(settings);
    assertThat(fxCopConf.slnFilePropertyKey()).isEqualTo("slnFilePath");
  }

  @Test
  public void check_properties_alternative_sln_no_pdb() {
	  thrown.expect(IllegalArgumentException.class);
	  thrown.expectMessage("Cannot find the .pdb file ");
	  
    Settings settings = new Settings();
    
    FxCopConfiguration fxCopConf = new FxCopConfiguration("", "", "", "", "", "fooFxCopCmdPathKey", "", "", "", "", "fooReportPathKey");
    fxCopConf.setAlternativeSln("src/test/resources/FxCopConfigGeneratorTests/TestApp1.sln");
    fxCopConf.checkProperties(settings);
  }
}

