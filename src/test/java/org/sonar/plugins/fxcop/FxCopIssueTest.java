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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FxCopIssueTest {

  @Test
  public void test() {
    FxCopIssue issue = new FxCopIssue(0, "S007", "path", null, null, "message");
    assertThat(issue.reportLine()).isEqualTo(0);
    assertThat(issue.ruleConfigKey()).isEqualTo("S007");
    assertThat(issue.path()).isEqualTo("path");
    assertThat(issue.file()).isNull();
    assertThat(issue.line()).isNull();
    assertThat(issue.message()).isEqualTo("message");

    issue = new FxCopIssue(42, "CA1000", null, "foo", 1, "bar");
    assertThat(issue.reportLine()).isEqualTo(42);
    assertThat(issue.ruleConfigKey()).isEqualTo("CA1000");
    assertThat(issue.path()).isNull();
    assertThat(issue.file()).isEqualTo("foo");
    assertThat(issue.line()).isEqualTo(1);
    assertThat(issue.message()).isEqualTo("bar");
  }

}
