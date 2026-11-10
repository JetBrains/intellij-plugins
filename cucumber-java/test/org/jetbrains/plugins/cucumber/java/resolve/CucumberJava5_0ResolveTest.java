// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.resolve;

import com.intellij.testFramework.fixtures.LightJavaProject;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;
import org.jetbrains.plugins.cucumber.resolve.CucumberResolveFixture;
import org.junit.jupiter.api.Test;

import static com.intellij.testFramework.fixtures.LightJavaFixturesKt.lightJavaProjectFixture;
import static org.jetbrains.plugins.cucumber.java.resolve.CucumberJavaResolveFixtures.cucumberJavaResolveFixture;

@TestApplication
public class CucumberJava5_0ResolveTest {
  private static final TestFixture<LightJavaProject> projectFixture = lightJavaProjectFixture(CucumberJavaTestUtil.createCucumber5ProjectDescriptor());
  private final TestFixture<CucumberResolveFixture> cucumber = cucumberJavaResolveFixture(projectFixture);

  @Test
  public void testHighlightingOK() {
    cucumber.get().init("stepResolve_cucumber_5");
    cucumber.get().getFixture().testHighlighting("ShoppingStepdefs.java");
  }

  @Test
  public void testResolveToNewAnnotation() {
    cucumber.get().init("stepResolve_cucumber_5");
    cucumber.get().checkReference("my step<caret> definition", "my_step_definition");
  }

  @Test
  public void testResolveToJava8StepDefinitions() {
    cucumber.get().init("stepResolve_cucumber_5");
    cucumber.get().checkReference("my jav<caret>a8 step", "Given");
  }

  @Test
  public void testResolveToJava8StepDefinitionsWithCast() {
    cucumber.get().init("stepResolve_cucumber_5");
    cucumber.get().checkReference("my jav<caret>a8 step with cast", "And");
  }

  @Test
  public void testResolveEscapeCharactersIsConsistent() {
    CucumberResolveFixture resolve = cucumber.get();
    resolve.init("stepResolve_cucumber_5");
    resolve.checkReference("my \\ <caret>step java 8", "When");
    resolve.checkReference("my \\ step<caret> java ann", "my_step_java_ann");
  }

  @Test
  public void testResolveToMethodWithColon() {
    cucumber.get().init("stepResolve_cucumber_5");
    cucumber.get().checkReference("step <caret><color>:", "my_step_with_colon");
  }

  @Test
  public void testResolveSingleCaret() {
    cucumber.get().init("stepResolve_cucumber_5");
    cucumber.get().checkReference("my another <caret>step definition with param \"<param>\"", "my_another_step_definition");
  }

  @Test
  public void testResolveDoubleCaret() {
    cucumber.get().init("stepResolve_cucumber_5");
    cucumber.get().checkReference("my another <caret>step definition with param \"<<param>>\"", "my_another_step_definition");
  }

  @Test
  public void testResolveScenarioParameterNotSurroundedWithCaret() {
    cucumber.get().init("stepResolve_cucumber_5");
    cucumber.get().checkReference("I expect inspection <caret>warning on <type> with messages 1", "iExpectInspection1");
  }

  @Test
  public void testResolveScenarioParameterSurroundedWithCaret() {
    cucumber.get().init("stepResolve_cucumber_5");
    cucumber.get().checkReference("I expect inspection <caret>warning on <<type>> with messages 2", "iExpectInspection2");
  }

  @Test
  public void testResolveScenarioParameterSurroundedWithManyCarets() {
    cucumber.get().init("stepResolve_cucumber_5");
    cucumber.get().checkReference("I expect inspection <caret>warning on <<<type>>> with messages 3", "iExpectInspection3");
  }

  @Test
  public void testResolveWithSeveralStepDefinitionAnnotations() {
    CucumberResolveFixture resolve = cucumber.get();
    resolve.init("stepResolve_cucumber_5");
    resolve.checkReference("first <caret>regex", "my_double_definition");
    resolve.checkReference("second <caret>regex", "my_double_definition");
  }

  // Test for IDEA-375195
  @Test
  public void testResolveEscapedBraces() {
    cucumber.get().doTest("escapedBraces", "cucumber in my belly {int}<caret> oops", "I_have_cucumbers_in_my_belly");
  }
}
