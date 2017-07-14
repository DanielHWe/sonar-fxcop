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
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.sonar.api.utils.command.Command;
import org.sonar.api.utils.command.CommandExecutor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class FxCopExecutor {

  private static final Logger LOG = Loggers.get(FxCopExecutor.class);
  private static final String EXECUTABLE = "FxCopCmd.exe";

  public void execute(String executable, String assemblies, File rulesetFile, File reportFile, int timeout, boolean aspnet, List<String> directories, List<String> references) {
    Command command = Command.create(getExecutable(executable))
      .addArgument("/file:" + assemblies)
      .addArgument("/ruleset:=" + rulesetFile.getAbsolutePath())
      .addArgument("/out:" + reportFile.getAbsolutePath())
      .addArgument("/outxsl:none")
      .addArgument("/forceoutput")
      .addArgument("/searchgac");

    if (aspnet) {
      command.addArgument("/aspnet");
    }

    for (String directory : directories) {
      command.addArgument("/directory:" + directory);
    }
    for (String reference : references) {
      command.addArgument("/reference:" + reference);
    }

    int exitCode = CommandExecutor.create().execute(
      command,
      TimeUnit.MINUTES.toMillis(timeout));

    LOG.info("FxCopCmd.exe ended with the exit code: " + exitCode);

    Preconditions.checkState((exitCode & 1) == 0,
      "The execution of \"" + executable + "\" failed and returned " + exitCode
        + " as exit code. See http://msdn.microsoft.com/en-us/library/bb429400(v=vs.80).aspx for details.");
  }

  /**
   * Handles deprecated property: "installDirectory", which gives the path to the directory only.
   */
  private static String getExecutable(String path) {
    return path.endsWith(EXECUTABLE) ? path : new File(path, EXECUTABLE).getAbsolutePath();
  }

}
