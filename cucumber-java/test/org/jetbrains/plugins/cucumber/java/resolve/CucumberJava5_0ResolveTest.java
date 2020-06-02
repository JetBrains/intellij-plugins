// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.resolve;

import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

public class CucumberJava5_0ResolveTest extends BaseCucumberJavaResolveTest {
  public void testResolveToNewAnnotation() {
    init("stepResolve_cucumber_5");
    checkReference("my step<caret> definition", "my_step_definition");
  }

  public void testResolveToJava8StepDefinitions() {
    init("stepResolve_cucumber_5");
    checkReference("my jav<caret>a8 step", "Given");
  }

  public void testResolveToMethodWithColon() {
    init("stepResolve_cucumber_5");
    checkReference("step <caret><color>:", "my_step_with_colon");
  }

  public void testResolveWithSeveralStepDefinitionAnnotations() {
    init("stepResolve_cucumber_5");
    checkReference("first <caret>regex", "my_double_definition");
    checkReference("second <caret>regex", "my_double_definition");
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumber5ProjectDescriptor();
  }
}
