/*
 * Copyright (C) 2012-2016 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package org.sonar.plugins.fxcop;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.squidbridge.rules.SqaleXmlLoader;

public class VbNetFxCopProvider {

  public static final String LANGUAGE_KEY = "vbnet";

  private static final String CATEGORY = "FxCop";
  private static final String SUBCATEGORY = "Deprecated";
  private static final String DEPRECATION_TEXT = "This deprecated property is not used anymore and so must be ignored when launching the SonarQube analysis of " +
    ".Net projects with help of the MSBuild Runner. If the old deprecated Visual Studio Bootstrapper plugin is still used along with SonarRunner to analyse .Net projects, " +
    "moving to the MSBuild Runner should be scheduled because one day the backward support of the Visual Studio Boostrapper plugin will be dropped.";

  private static final String FXCOP_ASSEMBLIES_PROPERTY_KEY = "sonar.vbnet.fxcop.assembly";
  private static final String FXCOP_PROJECT_PROPERTY_KEY = "sonar.vbnet.fxcop.project";
  private static final String FXCOP_SLN_FILE_PROPERTY_KEY = "sonar.cs.fxcop.slnFile";
  private static final String FXCOP_FXCOPCMD_PATH_PROPERTY_KEY = "sonar.vbnet.fxcop.fxCopCmdPath";
  private static final String FXCOP_TIMEOUT_PROPERTY_KEY = "sonar.vbnet.fxcop.timeoutMinutes";
  private static final String FXCOP_ASPNET_PROPERTY_KEY = "sonar.vbnet.fxcop.aspnet";
  private static final String FXCOP_DIRECTORIES_PROPERTY_KEY = "sonar.vbnet.fxcop.directories";
  private static final String FXCOP_REFERENCES_PROPERTY_KEY = "sonar.vbnet.fxcop.references";
  private static final String FXCOP_REPORT_PATH_PROPERTY_KEY = "sonar.vbnet.fxcop.reportPath";

  private static final FxCopConfiguration FXCOP_CONF = new FxCopConfiguration(
    LANGUAGE_KEY,
    "fxcop-vbnet",
    FXCOP_ASSEMBLIES_PROPERTY_KEY,
    FXCOP_PROJECT_PROPERTY_KEY,
    FXCOP_FXCOPCMD_PATH_PROPERTY_KEY,
    FXCOP_SLN_FILE_PROPERTY_KEY,
    FXCOP_TIMEOUT_PROPERTY_KEY,
    FXCOP_ASPNET_PROPERTY_KEY,
    FXCOP_DIRECTORIES_PROPERTY_KEY,
    FXCOP_REFERENCES_PROPERTY_KEY,
    FXCOP_REPORT_PATH_PROPERTY_KEY);

  private VbNetFxCopProvider() {
  }

  public static List extensions() {
    return ImmutableList.of(
      VbNetFxCopRulesDefinition.class,
      VbNetFxCopSensor.class,
      PropertyDefinition.builder(FXCOP_TIMEOUT_PROPERTY_KEY)
        .name(deprecatedName("FxCop execution timeout"))
        .description(deprecatedDescription("Time in minutes after which FxCop's execution should be interrupted if not finished"))
        .defaultValue("10")
        .category(CATEGORY)
        .subCategory(SUBCATEGORY)
        .onQualifiers(Qualifiers.PROJECT)
        .type(PropertyType.INTEGER)
        .build(),
      PropertyDefinition.builder(FXCOP_ASSEMBLIES_PROPERTY_KEY)
        .name(deprecatedName("Assembly to analyze"))
        .description(deprecatedDescription("Example: bin/Debug/MyProject.dll"))
        .category(CATEGORY)
        .subCategory(SUBCATEGORY)
        .onlyOnQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .build(),
      PropertyDefinition.builder(FXCOP_FXCOPCMD_PATH_PROPERTY_KEY)
        .name(deprecatedName("Path to FxCopCmd.exe"))
        .description(deprecatedDescription("Example: C:/Program Files (x86)/Microsoft Visual Studio 12.0/Team Tools/Static Analysis Tools/FxCop/FxCopCmd.exe"))
        .defaultValue("C:/Program Files (x86)/Microsoft Visual Studio 12.0/Team Tools/Static Analysis Tools/FxCop/FxCopCmd.exe")
        .category(CATEGORY)
        .subCategory(SUBCATEGORY)
        .onQualifiers(Qualifiers.PROJECT)
        .build(),
      PropertyDefinition.builder(FXCOP_ASPNET_PROPERTY_KEY)
        .name(deprecatedName("ASP.NET"))
        .description(deprecatedDescription("Whether or not to set the /aspnet flag when launching FxCopCmd.exe"))
        .defaultValue("false")
        .category(CATEGORY)
        .subCategory(SUBCATEGORY)
        .onlyOnQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .build(),
      PropertyDefinition.builder(FXCOP_DIRECTORIES_PROPERTY_KEY)
        .name(deprecatedName("Additional assemblies directories"))
        .description(deprecatedDescription("Comma-separated list of directories where FxCop should look for referenced assemblies. Example: c:/MyLibrary"))
        .category(CATEGORY)
        .subCategory(SUBCATEGORY)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .build(),
      PropertyDefinition.builder(FXCOP_REFERENCES_PROPERTY_KEY)
        .name(deprecatedName("Additional assemblies references"))
        .description(deprecatedDescription("Comma-separated list of referenced assemblies to pass to FxCop. Example: c:/MyLibrary.dll"))
        .category(CATEGORY)
        .subCategory(SUBCATEGORY)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .build());
  }

  private static String deprecatedDescription(String description) {
    return description + "<br /><br />" + DEPRECATION_TEXT;
  }

  private static String deprecatedName(String name) {
    return "Deprecated - " + name;
  }

  public static class VbNetFxCopRulesDefinition extends FxCopRulesDefinition {

    public VbNetFxCopRulesDefinition() {
      super(FXCOP_CONF, repo -> SqaleXmlLoader.load(repo, "/com/sonar/sqale/fxcop-vbnet.xml"));
    }

  }

  public static class VbNetFxCopSensor extends FxCopSensor {

    public VbNetFxCopSensor() {
      super(FXCOP_CONF);
    }

  }

}
