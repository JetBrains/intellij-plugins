// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.resolve;

import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

public class CucumberJava5_5ResolveTest extends BaseCucumberJavaResolveTest {
  public void testResolveAnnotatedParameterType() {
    init("stepResolve_cucumber_5", "ShoppingStepdefs.java");

    checkReference("step {col<caret>or}", "color");
  }

  public void testOptionalsWithCyrillic() {
    //noinspection NonAsciiCharacters
    doTest("optionalsWithCyrillic", "суфф<caret>икс", "cucumberExpressionWithOptional");
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumberProjectDescriptor("5.5");
  }
}
