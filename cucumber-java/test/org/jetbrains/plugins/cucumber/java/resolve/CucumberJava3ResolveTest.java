// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.resolve;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.project.IntelliJProjectConfiguration;
import com.intellij.testFramework.IdeaTestUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;


public class CucumberJava3ResolveTest extends BaseCucumberJavaResolveTest {
  public void testHighlightingOK_1() {
    init("stepResolve_ParameterType");
    myFixture.testHighlighting("ParameterTypeSteps.java");
  }

  public void testHighlightingOK_2() {
    init("stepResolveJava8CucumberExpressions");
    myFixture.testHighlighting(
      "ParameterTypeSteps.java",
      "TargetStatusSteps.java",
      "io/cucumber/cucumberexpressions/ParameterType.java"
    );
  }

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

  public void testStepResolveJava8CucumberExpressions() {
    init("stepResolveJava8CucumberExpressions");

    checkReference("the string \"test\" is in the d<caret>ummy repository", "Given");
  }

  public void testJava8MethodReference() {
    init("stepResolveJava8CucumberExpressions");

    checkReference("the list should contain exac<caret>tly 1 entries", "Then");
  }

  public void testResolveStepDefinitionRequiringEscaping() {
    init("stepResolve_ParameterType");

    checkReference("| st<caret>ep \"", "my_test_step");
  }

  public void testResolveStepDefinitionWithParenthesis() {
    init("stepResolve_ParameterType");

    checkReference("Given I have pa<caret>renthes(s)", "my_test_step_with_parenthesis");
  }

  public void testAnonymousParameterType() {
    init("stepResolve_ParameterType");

    checkReference("step with any<caret>thing", "anonymousParameterType");
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return new DefaultLightProjectDescriptor(IdeaTestUtil::getMockJdk11) {
      @Override
      public void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
        CucumberJavaTestUtil.attachCucumberCore3(model);
        CucumberJavaTestUtil.attachStandardCucumberLibraries(model);

        var libraryRoots = IntelliJProjectConfiguration.getModuleLibrary("intellij.cucumber.java", "cucumber-java8-3");
        PsiTestUtil.addProjectLibrary(model, "cucumber-java8", libraryRoots.getClassesPaths());
      }
    };
  }
}
