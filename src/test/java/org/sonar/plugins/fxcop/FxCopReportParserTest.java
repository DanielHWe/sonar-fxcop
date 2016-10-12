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
import java.util.Iterator;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

public class FxCopReportParserTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void valid() {
    List<FxCopIssue> issues = new FxCopReportParser().parse(new File("src/test/resources/FxCopReportParserTest/valid.xml"));

    Iterator<FxCopIssue> it = issues.iterator();

    FxCopIssue issue = it.next();
    assertThat(issue.reportLine()).isEqualTo(9);
    assertThat(issue.ruleConfigKey()).isEqualTo("CA2210");
    assertThat(issue.path()).isNull();
    assertThat(issue.file()).isNull();
    assertThat(issue.line()).isNull();
    assertThat(issue.message()).isEqualTo("Sign 'MyLibrary.dll' with a strong name key.");

    issue = it.next();
    assertThat(issue.reportLine()).isEqualTo(12);
    assertThat(issue.ruleConfigKey()).isEqualTo("CA1014");

    issue = it.next();
    assertThat(issue.reportLine()).isEqualTo(23);
    assertThat(issue.ruleConfigKey()).isEqualTo("CA1704");
    assertThat(issue.path()).isEqualTo("c:\\Users\\SonarSource\\Documents\\Visual Studio 2013\\Projects\\CSharpPlayground\\MyLibrary");
    assertThat(issue.file()).isEqualTo("Class1.cs");
    assertThat(issue.line()).isEqualTo(12);
    assertThat(issue.message()).isEqualTo("In method 'Class1.Add(int, int)', consider providing a more meaningful name than parameter name 'a'.");

    issue = it.next();
    assertThat(issue.reportLine()).isEqualTo(26);
    assertThat(issue.ruleConfigKey()).isEqualTo("CA1704");

    // issue on line 29 is suppressed

    assertThat(it.hasNext()).isFalse();
  }

  @Test
  public void invalid_line() {
    thrown.expectMessage("Expected an integer instead of \"foo\" for the attribute \"Line\"");
    thrown.expectMessage("invalid_line.xml at line 9");

    new FxCopReportParser().parse(new File("src/test/resources/FxCopReportParserTest/invalid_line.xml"));
  }

  @Test
  public void missing_checkid() {
    thrown.expectMessage("Missing attribute \"CheckId\" in element <Message>");
    thrown.expectMessage("missing_checkid.xml at line 8");

    new FxCopReportParser().parse(new File("src/test/resources/FxCopReportParserTest/missing_checkid.xml"));
  }

  @Test
  public void non_existing() {
    thrown.expectMessage("java.io.FileNotFoundException");
    thrown.expectMessage("non_existing.xml");

    new FxCopReportParser().parse(new File("src/test/resources/FxCopReportParserTest/non_existing.xml"));
  }

}
