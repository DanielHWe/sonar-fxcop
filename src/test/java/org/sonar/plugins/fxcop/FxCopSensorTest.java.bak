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

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.rule.RuleKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FxCopSensorTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testDescriptor() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    new FxCopSensor(new FxCopConfiguration("foo", "foo-fxcop", "", "", "", "", "", "", "")).describe(descriptor);
    assertThat(descriptor.name()).isEqualTo("FxCop (foo)");
  }

  @Test
  public void analyze_execute_fxcop() throws Exception {
    Path baseDir = temp.newFolder().toPath();
    SensorContextTester context = SensorContextTester.create(baseDir);

    FxCopConfiguration fxCopConf = mock(FxCopConfiguration.class);
    when(fxCopConf.languageKey()).thenReturn("foo");
    when(fxCopConf.repositoryKey()).thenReturn("foo-fxcop");
    when(fxCopConf.assemblyPropertyKey()).thenReturn("assemblyKey");
    when(fxCopConf.fxCopCmdPropertyKey()).thenReturn("fxcopcmdPath");
    when(fxCopConf.timeoutPropertyKey()).thenReturn("timeout");
    when(fxCopConf.aspnetPropertyKey()).thenReturn("aspnet");
    when(fxCopConf.directoriesPropertyKey()).thenReturn("directories");
    when(fxCopConf.referencesPropertyKey()).thenReturn("references");

    FxCopSensor sensor = new FxCopSensor(fxCopConf);
    context.settings().setProperty("assemblyKey", "MyLibrary.dll");
    context.settings().setProperty("fxcopcmdPath", "FxCopCmd.exe");
    context.settings().setProperty("timeout", "42");
    context.settings().setProperty("aspnet", "true");
    context.settings().setProperty("directories", " c:/,,  d:/ ");

    ActiveRulesBuilder activeRulesBuilder = new ActiveRulesBuilder();
    activeRulesBuilder.create(RuleKey.of("foo-fxcop", "_CA0000")).setInternalKey("CA0000").activate();
    activeRulesBuilder.create(RuleKey.of("foo-fxcop", "_CA1000")).setInternalKey("CA1000").activate();
    activeRulesBuilder.create(RuleKey.of("foo-fxcop", "CustomRuleTemplate")).activate();
    activeRulesBuilder.create(RuleKey.of("foo-fxcop", "CustomRuleTemplate_42")).setParam("CheckId", "CR1000").activate();

    context.setActiveRules(activeRulesBuilder.build());

    FxCopExecutor executor = mock(FxCopExecutor.class);

    File workingDir = new File(new File("target/FxCopSensorTest/working-dir").getAbsolutePath());
    context.fileSystem().setWorkDir(workingDir);

    DefaultInputFile class3InputFile = new DefaultInputFile(context.module().key(), "Class3.cs").setLanguage("foo");
    // Class4 missing on purpose
    DefaultInputFile class5InputFile = new DefaultInputFile(context.module().key(), "Class5.cs").setLanguage("foo");
    DefaultInputFile class6InputFile = new DefaultInputFile(context.module().key(), "Class6.cs").setLanguage("foo").initMetadata("1\n2\n3\n4\n5\n6");
    DefaultInputFile class7InputFile = new DefaultInputFile(context.module().key(), "Class7.cs").setLanguage("bar").initMetadata("1\n2\n3\n4\n5\n6\n7");
    // Class8 has language "bar"
    DefaultInputFile class8InputFile = new DefaultInputFile(context.module().key(), "Class8.cs").setLanguage("foo").initMetadata("1\n2\n3\n4\n5\n6\n7\n8");

    context.fileSystem().add(class3InputFile);
    // Class4 missing on purpose
    context.fileSystem().add(class5InputFile);
    context.fileSystem().add(class6InputFile);
    context.fileSystem().add(class7InputFile);
    context.fileSystem().add(class8InputFile);

    FxCopRulesetWriter writer = mock(FxCopRulesetWriter.class);

    FxCopReportParser parser = mock(FxCopReportParser.class);
    when(parser.parse(new File(workingDir, "fxcop-report.xml"))).thenReturn(
      ImmutableList.of(
        new FxCopIssue(100, "CA0000", null, "Class1.cs", 1, "Dummy message"), // no path -> project
        new FxCopIssue(200, "CA0000", baseDir.toString(), null, 2, "Dummy message 2"), // no filename -> project
        new FxCopIssue(300, "CA0000", baseDir.toString(), "Class3.cs", null, "Dummy message"), // no line -> on file
        // no input file but not a source file -> on project
        new FxCopIssue(400, "CA0000", baseDir.toString(), "Class4.dll", 4, "First message"),
        // no input file but is a source file -> skipped
        new FxCopIssue(400, "CA0000", baseDir.toString(), "Class4.cs", 4, "First message 2"),
        new FxCopIssue(500, "CA0000", baseDir.toString(), "Class5.cs", 0, "Second message"), // all good but line 0 -> on file
        new FxCopIssue(600, "CA1000", baseDir.toString(), "Class6.cs", 6, "Third message"), // all good -> on file+line
        new FxCopIssue(700, "CA0000", baseDir.toString(), "Class7.cs", 7, "Fourth message"), // language "bar" -> on file+line
        new FxCopIssue(800, "CR1000", baseDir.toString(), "Class8.cs", 8, "Fifth message"))); // all good -> on file+line

    sensor.analyse(writer, parser, executor, context);

    verify(writer).write(ImmutableList.of("CA0000", "CA1000", "CR1000"), new File(workingDir, "fxcop-sonarqube.ruleset"));
    verify(executor).execute("FxCopCmd.exe", "MyLibrary.dll", new File(workingDir, "fxcop-sonarqube.ruleset"), new File(workingDir, "fxcop-report.xml"), 42, true,
      ImmutableList.of("c:/", "d:/"), ImmutableList.<String>of());

    assertThat(context.allIssues())
      .extracting("ruleKey", "primaryLocation.component", "primaryLocation.textRange.start.line", "primaryLocation.message")
      .containsOnly(
        tuple(RuleKey.of("foo-fxcop", "_CA0000"), context.module(), null, "Dummy message"),
        tuple(RuleKey.of("foo-fxcop", "_CA0000"), context.module(), null, "Dummy message 2"),
        tuple(RuleKey.of("foo-fxcop", "_CA0000"), class3InputFile, null, "Dummy message"),
        tuple(RuleKey.of("foo-fxcop", "_CA0000"), context.module(), null, baseDir.resolve("Class4.dll").toString() + " line 4: First message"),
        tuple(RuleKey.of("foo-fxcop", "_CA0000"), class5InputFile, null, "Second message"),
        tuple(RuleKey.of("foo-fxcop", "_CA1000"), class6InputFile, 6, "Third message"),
        tuple(RuleKey.of("foo-fxcop", "_CA0000"), class7InputFile, 7, "Fourth message"),
        tuple(RuleKey.of("foo-fxcop", "CustomRuleTemplate_42"), class8InputFile, 8, "Fifth message"));

  }

  @Test
  public void analyze_reuse_report() throws Exception {
    Path baseDir = temp.newFolder().toPath();
    SensorContextTester context = SensorContextTester.create(baseDir);

    FxCopConfiguration fxCopConf = mock(FxCopConfiguration.class);
    when(fxCopConf.repositoryKey()).thenReturn("foo-fxcop");
    when(fxCopConf.reportPathPropertyKey()).thenReturn("reportPath");

    FxCopSensor sensor = new FxCopSensor(fxCopConf);

    File reportFile = new File("src/test/resources/FxCopSensorTest/fxcop-report.xml");
    context.settings().setProperty("reportPath", reportFile.getAbsolutePath());

    FxCopRulesetWriter writer = mock(FxCopRulesetWriter.class);
    FxCopReportParser parser = mock(FxCopReportParser.class);
    FxCopExecutor executor = mock(FxCopExecutor.class);

    sensor.analyse(writer, parser, executor, context);

    verify(writer, never()).write(anyListOf(String.class), any(File.class));
    verify(executor, never()).execute(
      anyString(), anyString(), any(File.class), any(File.class), anyInt(), anyBoolean(), anyListOf(String.class), anyListOf(String.class));

    verify(parser).parse(new File(reportFile.getAbsolutePath()));
  }

  @Test
  public void check_properties() throws IOException {
    assumeTrue(System.getProperty("os.name").startsWith("Windows"));
    thrown.expectMessage("No FxCop analysis has been performed on this project");

    FxCopConfiguration fxCopConf = new FxCopConfiguration("", "", "fooAssemblyKey", "", "", "", "", "", "");
    new FxCopSensor(fxCopConf).execute(SensorContextTester.create(temp.newFolder()));
  }

}
