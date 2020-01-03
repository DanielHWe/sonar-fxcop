/*
 * SonarQube C# Plugin
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

import java.util.List;
import org.junit.Test;
import org.sonar.api.Plugin;
import org.sonar.api.SonarRuntime;
import org.sonar.api.utils.Version;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class FxCopPluginTest {

  @Test
  public void getExtensions() {
    Plugin.Context context = new Plugin.Context((SonarRuntime) mock(SonarRuntime.class));
    new FxCopPlugin().define(context);

    List extensions = context.getExtensions();

    assertThat(extensions).hasSize(
      CSharpFxCopProvider.extensions().size()
        + VbNetFxCopProvider.extensions().size());
  }

}
