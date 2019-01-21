// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CucumberJavaUtilTest {
  @Test
  public void testIsCucumberExpression() {
    assertTrue(CucumberJavaUtil.isCucumberExpression("strings are cukexp by default"));
    assertFalse(CucumberJavaUtil.isCucumberExpression("^definitely a regexp$"));
    assertFalse(CucumberJavaUtil.isCucumberExpression("/surely a regexp/"));
    assertFalse(CucumberJavaUtil.isCucumberExpression("this (.+) like a regexp"));
    assertTrue(CucumberJavaUtil.isCucumberExpression("this look(s) like a cukexp"));
    assertFalse(CucumberJavaUtil.isCucumberExpression("(\\)text)"));
    assertTrue(CucumberJavaUtil.isCucumberExpression("\\(text)"));
  }
}
