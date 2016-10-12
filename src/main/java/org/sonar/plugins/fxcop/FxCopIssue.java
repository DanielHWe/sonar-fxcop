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

import javax.annotation.Nullable;

public class FxCopIssue {

  private final int reportLine;
  private final String ruleConfigKey;
  private final String path;
  private final String file;
  private final Integer line;
  private final String message;

  public FxCopIssue(int reportLine, String ruleConfigKey, @Nullable String path, @Nullable String file, @Nullable Integer line, String message) {
    this.reportLine = reportLine;
    this.ruleConfigKey = ruleConfigKey;
    this.path = path;
    this.file = file;
    this.line = line;
    this.message = message;
  }

  public int reportLine() {
    return reportLine;
  }

  public String ruleConfigKey() {
    return ruleConfigKey;
  }

  @Nullable
  public String path() {
    return path;
  }

  @Nullable
  public String file() {
    return file;
  }

  @Nullable
  public Integer line() {
    return line;
  }

  public String message() {
    return message;
  }

}
