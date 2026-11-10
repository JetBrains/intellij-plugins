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
public class CucumberJava4_8ResolveTest {
  private static final TestFixture<LightJavaProject> projectFixture = lightJavaProjectFixture(CucumberJavaTestUtil.createCucumber4_8ProjectDescriptor());
  private final TestFixture<CucumberResolveFixture> cucumber = cucumberJavaResolveFixture(projectFixture);

  @Test
  public void testHighlightingOK() {
    cucumber.get().init("stepResolve_01");
    cucumber.get().getFixture().testHighlighting("ShoppingStepdefs.java");
  }

  @Test
  public void testResolveToNewAnnotation() {
    cucumber.get().init("stepResolve_cucumber_4_8");
    cucumber.get().checkReference("my step<caret> definition", "my_step_definition");
  }

  @Test
  public void testResolveToDeprecatedAnnotation() {
    cucumber.get().init("stepResolve_ParameterType");
    cucumber.get().checkReference("tod<caret>ay", "step_method");
  }

  @Test
  public void testResolveToJava8StepDefinitions() {
    cucumber.get().init("stepResolve_cucumber_4_8");
    cucumber.get().checkReference("my jav<caret>a8 step", "Given");
  }

  @Test
  public void testResolveEnumParameterType() {
    cucumber.get().init("stepResolve_cucumber_4_8");
    cucumber.get().checkReference("smth w<caret>ith ONE", "smthWithMyEnum");
  }
}
