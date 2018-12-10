// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.resolve;

import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

public class CucumberJava1ResolveTest extends BaseCucumberJavaResolveTest {
  public void testExactStepMatching() {
    init("stepResolve_01");
    checkReference("test sho<caret>uld pass", null);
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumber1ProjectDescriptor();
  }
}
