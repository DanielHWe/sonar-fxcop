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
import org.sonar.api.config.Settings;

public class FxCopConfiguration {

  private static final String DEPRECATED_FXCOPCMD_PATH_PROPERTY_KEY = "sonar.fxcop.installDirectory";
  private static final String DEPRECATED_TIMEOUT_MINUTES_PROPERTY_KEY = "sonar.fxcop.timeoutMinutes";

  private final String languageKey;
  private final String repositoryKey;
  private final String assemblyPropertyKey;
  private String fxCopCmdPropertyKey;
  private String timeoutPropertyKey;
  private final String aspnetPropertyKey;
  private final String directoriesPropertyKey;
  private final String referencesPropertyKey;
  private final String reportPathPropertyKey;

  public FxCopConfiguration(String languageKey, String repositoryKey, String assemblyPropertyKey, String fxCopCmdPropertyKey, String timeoutPropertyKey, String aspnetPropertyKey,
    String directoriesPropertyKey, String referencesPropertyKey,
    String reportPathPropertyKey) {
    this.languageKey = languageKey;
    this.repositoryKey = repositoryKey;
    this.assemblyPropertyKey = assemblyPropertyKey;
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

  public void checkProperties(Settings settings) {
    if (settings.hasKey(reportPathPropertyKey)) {
      checkReportPathProperty(settings);
    } else {
      checkMandatoryProperties(settings);
      checkAssemblyProperty(settings);
      checkFxCopCmdPathProperty(settings);
      checkTimeoutProeprty(settings);
    }
  }

  private void checkMandatoryProperties(Settings settings) {
    if (!settings.hasKey(assemblyPropertyKey)) {
      throw new IllegalArgumentException("No FxCop analysis has been performed on this project, whereas it contains " + languageKey() + " files: " +
        "Verify that you are using the latest version of the SonarQube Scanner for MSBuild, and if you do, please report a bug. " +
        "In the short term, you can disable all FxCop rules from your quality profile to get rid of this error.");
    }
  }

  private void checkAssemblyProperty(Settings settings) {
    String assemblyPath = settings.getString(assemblyPropertyKey);

    File assemblyFile = new File(assemblyPath);
    Preconditions.checkArgument(
      assemblyFile.isFile(),
      "Cannot find the assembly \"" + assemblyFile.getAbsolutePath() + "\" provided by the property \"" + assemblyPropertyKey + "\".");

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

    File file = new File(value);
    Preconditions.checkArgument(
      file.isFile(),
      "Cannot find the FxCopCmd executable \"" + file.getAbsolutePath() + "\" provided by the property \"" + fxCopCmdPropertyKey + "\".");
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
      "Cannot find the FxCop report \"" + file.getAbsolutePath() + "\" provided by the property \"" + reportPathPropertyKey + "\".");
  }

}
