// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.inspections;

import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

public class CucumberJava3CreateStepTest extends AbstractCucumberJavaCreateStepTest {
  public void testCucumberExpressionsStepDefinition() {
    doTest(false);
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumber3ProjectDescriptor();
  }
}
