// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.resolve;

import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

public class CucumberJava3ResolveTest extends BaseCucumberJavaResolveTest {
  public void testResolveOfStepWithParameterType() {
    init("stepResolve_ParameterType");
    checkReference("tod<caret>ay", "step_method");
    checkReference("in<caret>t", "step_method");
    checkReference("floa<caret>t", "step_method");
    checkReference("wor<caret>d", "step_method");
    checkReference("strin<caret>g", "step_string_method");
  }

  public void testResolveOfStepWithAdditionalParameterType() {
    init("stepResolve_ParameterType");
    checkReference("bigin<caret>teger", "step_method");
    checkReference("bigde<caret>cimal", "step_method");
    checkReference("sho<caret>rt", "step_method");
    checkReference("by<caret>te", "step_method");
    checkReference("lo<caret>ng", "step_method");
    checkReference("dou<caret>ble", "step_method");
  }

  public void testResolveOfExpressionWithNotNecessaryGroup() {
    init("stepResolve_ParameterType");

    checkReference("I have 10 cucum<caret>bers in my belly", "iHaveCucumbersInMyBelly");
    checkReference("I have 1 cucumb<caret>er in my belly", "iHaveCucumbersInMyBelly");
  }

  public void testResolveWithDollar() {
    init("stepResolve_ParameterType");

    checkReference("I ha<caret>ve $10", "iHaveDollars");
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumber3ProjectDescriptor();
  }
}
