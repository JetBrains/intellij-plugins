// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java;

import org.junit.Test;

import static org.junit.Assert.*;

public class CucumberJavaUtilTest {
  @Test
  public void testIsCucumberExpression() {
    assertTrue(CucumberJavaUtil.isCucumberExpression("strings are cukexp by default"));
    assertFalse(CucumberJavaUtil.isCucumberExpression("^definitely a regexp$"));
    assertFalse(CucumberJavaUtil.isCucumberExpression("^definitely a regexp"));
    assertFalse(CucumberJavaUtil.isCucumberExpression("definitely a regexp$"));
    assertFalse(CucumberJavaUtil.isCucumberExpression("/surely a regexp/"));
    assertTrue(CucumberJavaUtil.isCucumberExpression("this look(s) like a cukexp"));
    assertTrue(CucumberJavaUtil.isCucumberExpression("\\(text)"));
  }

  @Test
  public void testGetCucumberMainClass() {
    assertEquals("cucumber.cli.Main", CucumberJavaUtil.getCucumberMainClass("1"));
    assertEquals("cucumber.api.cli.Main", CucumberJavaUtil.getCucumberMainClass("1.1"));
    assertEquals("cucumber.api.cli.Main", CucumberJavaUtil.getCucumberMainClass("1.2"));
    assertEquals("cucumber.api.cli.Main", CucumberJavaUtil.getCucumberMainClass("2"));
    assertEquals("cucumber.api.cli.Main", CucumberJavaUtil.getCucumberMainClass("2.1"));
    assertEquals("cucumber.api.cli.Main", CucumberJavaUtil.getCucumberMainClass("2.2"));
    assertEquals("cucumber.api.cli.Main", CucumberJavaUtil.getCucumberMainClass("2.3"));
    assertEquals("cucumber.api.cli.Main", CucumberJavaUtil.getCucumberMainClass("2.4"));
    assertEquals("cucumber.api.cli.Main", CucumberJavaUtil.getCucumberMainClass("3"));
    assertEquals("cucumber.api.cli.Main", CucumberJavaUtil.getCucumberMainClass("4"));
    assertEquals("cucumber.api.cli.Main", CucumberJavaUtil.getCucumberMainClass("4.1"));
    assertEquals("cucumber.api.cli.Main", CucumberJavaUtil.getCucumberMainClass("4.2"));
    assertEquals("cucumber.api.cli.Main", CucumberJavaUtil.getCucumberMainClass("4.3"));
    assertEquals("cucumber.api.cli.Main", CucumberJavaUtil.getCucumberMainClass("4.4"));
    assertEquals("io.cucumber.core.cli.Main", CucumberJavaUtil.getCucumberMainClass("4.5"));
    assertEquals("io.cucumber.core.cli.Main", CucumberJavaUtil.getCucumberMainClass("4.6"));
    assertEquals("io.cucumber.core.cli.Main", CucumberJavaUtil.getCucumberMainClass("4.7"));
  }
}
