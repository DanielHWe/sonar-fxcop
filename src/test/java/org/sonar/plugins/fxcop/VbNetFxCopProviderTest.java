/*
 * Copyright (C) 2012-2016 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package org.sonar.plugins.fxcop;

import com.google.common.collect.ImmutableSet;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.plugins.fxcop.VbNetFxCopProvider.VbNetFxCopSensor;

import static org.assertj.core.api.Assertions.assertThat;

public class VbNetFxCopProviderTest {

  @Test
  public void private_constructor() throws Exception {
    Constructor constructor = VbNetFxCopProvider.class.getDeclaredConstructor();
    assertThat(constructor.isAccessible()).isFalse();
    constructor.setAccessible(true);
    constructor.newInstance();
  }

  @Test
  public void test() {
    assertThat(nonProperties(VbNetFxCopProvider.extensions())).containsOnly(
      VbNetFxCopProvider.VbNetFxCopRulesDefinition.class,
      VbNetFxCopSensor.class);
    assertThat(propertyKeys(VbNetFxCopProvider.extensions())).containsOnly(
      "sonar.vbnet.fxcop.assembly",
      "sonar.vbnet.fxcop.timeoutMinutes",
      "sonar.vbnet.fxcop.fxCopCmdPath",
      "sonar.vbnet.fxcop.aspnet",
      "sonar.vbnet.fxcop.directories",
      "sonar.vbnet.fxcop.references");
  }

  private static Set nonProperties(List extensions) {
    ImmutableSet.Builder builder = ImmutableSet.builder();
    for (Object extension : extensions) {
      if (!(extension instanceof PropertyDefinition)) {
        builder.add(extension);
      }
    }
    return builder.build();
  }

  private static Set<String> propertyKeys(List extensions) {
    ImmutableSet.Builder<String> builder = ImmutableSet.builder();
    for (Object extension : extensions) {
      if (extension instanceof PropertyDefinition) {
        PropertyDefinition property = (PropertyDefinition) extension;
        builder.add(property.key());
      }
    }
    return builder.build();
  }

}
