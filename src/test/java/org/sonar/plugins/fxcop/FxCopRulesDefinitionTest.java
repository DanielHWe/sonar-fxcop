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

import java.util.List;
import org.junit.Test;
import org.sonar.api.server.rule.RulesDefinition.Context;
import org.sonar.api.server.rule.RulesDefinition.Repository;
import org.sonar.api.server.rule.RulesDefinition.Rule;
import org.sonar.plugins.fxcop.FxCopRulesDefinition.FxCopRulesDefinitionSqaleLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class FxCopRulesDefinitionTest {

  @Test
  public void test_cs() {
    Context context = new Context();
    assertThat(context.repositories()).isEmpty();

    FxCopRulesDefinition repo = new FxCopRulesDefinition(new FxCopConfiguration("cs", "cs-fxcop", "", "", "", "", "", "", "", "", ""), mock(FxCopRulesDefinitionSqaleLoader.class));
    repo.define(context);

    assertThat(context.repositories()).hasSize(1);
    Repository repository = context.repository("cs-fxcop");
    assertThat(repository.language()).isEqualTo("cs");
    List<Rule> rules = repository.rules();
    assertThat(rules).hasSize(233);
    for (Rule rule : rules) {
      assertThat(rule.key()).isNotNull();
      assertThat(rule.name()).isNotNull();
      assertThat(rule.htmlDescription()).isNotNull();
    }

    assertThat(containsCustomRule(rules)).isTrue();
    assertThat(containsTags(rules)).isTrue();
  }

  @Test
  public void test_vbnet() {
    Context context = new Context();
    assertThat(context.repositories()).isEmpty();

    FxCopRulesDefinition repo = new FxCopRulesDefinition(new FxCopConfiguration("vbnet", "vbnet-fxcop", "", "", "", "", "", "", "", "", ""), mock(FxCopRulesDefinitionSqaleLoader.class));
    repo.define(context);

    assertThat(context.repositories()).hasSize(1);
    Repository repository = context.repository("vbnet-fxcop");
    assertThat(repository.language()).isEqualTo("vbnet");
    List<Rule> rules = repository.rules();
    assertThat(rules).hasSize(233);
    for (Rule rule : rules) {
      assertThat(rule.key()).isNotNull();
      assertThat(rule.name()).isNotNull();
      assertThat(rule.htmlDescription()).isNotNull();
    }

    assertThat(containsCustomRule(rules)).isTrue();
  }

  private static boolean containsCustomRule(List<Rule> rules) {
    for (Rule rule : rules) {
      if (rule.template() && "CustomRuleTemplate".equals(rule.key())) {
        return true;
      }
    }

    return false;
  }

  private boolean containsTags(List<Rule> rules) {
    for (Rule rule : rules) {
      if (!rule.tags().isEmpty()) {
        return true;
      }
    }

    return false;
  }

}
