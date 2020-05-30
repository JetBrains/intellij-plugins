// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.resolve;

import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

public class CucumberJava4_5ResolveTest extends BaseCucumberJavaResolveTest {
  public void testResolveToNewAnnotation() {
    init("stepResolve_cucumber_4_5");
    checkReference("my step<caret> definition", "my_step_definition");
  }

  public void testResolveToDeprecatedAnnotation() {
    init("stepResolve_ParameterType");
    checkReference("tod<caret>ay", "step_method");
  }

  public void testResolveToJava8StepDefinitions() {
    init("stepResolve_cucumber_4_5");
    checkReference("my jav<caret>a8 step", "Given");
  }

  public void testResolveEnumParameterType() {
    init("stepResolve_cucumber_4_5");
    checkReference("smth w<caret>ith ONE", "smthWithMyEnum");
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumber4_5ProjectDescriptor();
  }
}
