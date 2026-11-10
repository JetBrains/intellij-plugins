// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.resolve;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.project.IntelliJProjectConfiguration;
import com.intellij.testFramework.IdeaTestUtil;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightJavaProject;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;
import org.jetbrains.plugins.cucumber.resolve.CucumberResolveFixture;
import org.junit.jupiter.api.Test;

import static com.intellij.testFramework.fixtures.LightJavaFixturesKt.lightJavaProjectFixture;
import static org.jetbrains.plugins.cucumber.java.resolve.CucumberJavaResolveFixtures.cucumberJavaResolveFixture;

@TestApplication
public class CucumberJava3ResolveTest {
  private static final TestFixture<LightJavaProject> projectFixture = lightJavaProjectFixture(createDescriptor());
  private final TestFixture<CucumberResolveFixture> cucumber = cucumberJavaResolveFixture(projectFixture);

  @Test
  public void testHighlightingOK_1() {
    cucumber.get().init("stepResolve_ParameterType");
    cucumber.get().getFixture().testHighlighting("ParameterTypeSteps.java");
  }

  @Test
  public void testHighlightingOK_2() {
    cucumber.get().init("stepResolveJava8CucumberExpressions");
    cucumber.get().getFixture().testHighlighting(
      "ParameterTypeSteps.java",
      "TargetStatusSteps.java",
      "io/cucumber/cucumberexpressions/ParameterType.java"
    );
  }

  @Test
  public void testResolveOfStepWithParameterType() {
    CucumberResolveFixture resolve = cucumber.get();
    resolve.init("stepResolve_ParameterType");
    resolve.checkReference("tod<caret>ay", "step_method");
    resolve.checkReference("in<caret>t", "step_method");
    resolve.checkReference("floa<caret>t", "step_method");
    resolve.checkReference("wor<caret>d", "step_method");
    resolve.checkReference("strin<caret>g", "step_string_method");
  }

  @Test
  public void testResolveOfStepWithAdditionalParameterType() {
    CucumberResolveFixture resolve = cucumber.get();
    resolve.init("stepResolve_ParameterType");
    resolve.checkReference("bigin<caret>teger", "step_method");
    resolve.checkReference("bigde<caret>cimal", "step_method");
    resolve.checkReference("sho<caret>rt", "step_method");
    resolve.checkReference("by<caret>te", "step_method");
    resolve.checkReference("lo<caret>ng", "step_method");
    resolve.checkReference("dou<caret>ble", "step_method");
  }

  @Test
  public void testResolveOfExpressionWithNotNecessaryGroup() {
    CucumberResolveFixture resolve = cucumber.get();
    resolve.init("stepResolve_ParameterType");

    resolve.checkReference("I have 10 cucum<caret>bers in my belly", "iHaveCucumbersInMyBelly");
    resolve.checkReference("I have 1 cucumb<caret>er in my belly", "iHaveCucumbersInMyBelly");
  }

  @Test
  public void testResolveWithDollar() {
    cucumber.get().init("stepResolve_ParameterType");

    cucumber.get().checkReference("I ha<caret>ve $10", "iHaveDollars");
  }

  @Test
  public void testStepResolveJava8CucumberExpressions() {
    cucumber.get().init("stepResolveJava8CucumberExpressions");

    cucumber.get().checkReference("the string \"test\" is in the d<caret>ummy repository", "Given");
  }

  @Test
  public void testJava8MethodReference() {
    cucumber.get().init("stepResolveJava8CucumberExpressions");

    cucumber.get().checkReference("the list should contain exac<caret>tly 1 entries", "Then");
  }

  @Test
  public void testResolveStepDefinitionRequiringEscaping() {
    cucumber.get().init("stepResolve_ParameterType");

    cucumber.get().checkReference("| st<caret>ep \"", "my_test_step");
  }

  @Test
  public void testResolveStepDefinitionWithParenthesis() {
    cucumber.get().init("stepResolve_ParameterType");

    cucumber.get().checkReference("Given I have pa<caret>renthes(s)", "my_test_step_with_parenthesis");
  }

  @Test
  public void testAnonymousParameterType() {
    cucumber.get().init("stepResolve_ParameterType");

    cucumber.get().checkReference("step with any<caret>thing", "anonymousParameterType");
  }

  private static DefaultLightProjectDescriptor createDescriptor() {
    return new DefaultLightProjectDescriptor(IdeaTestUtil::getMockJdk11) {
      @Override
      public void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
        super.configureModule(module, model, contentEntry);
        CucumberJavaTestUtil.attachCucumberCore3(model);
        CucumberJavaTestUtil.attachStandardCucumberLibraries(model);

        var libraryRoots = IntelliJProjectConfiguration.getModuleLibrary("intellij.cucumber.java.tests", "cucumber-java8-3");
        PsiTestUtil.addProjectLibrary(model, "cucumber-java8", libraryRoots.getClassesPaths());
      }
    };
  }
}
