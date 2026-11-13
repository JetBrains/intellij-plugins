// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.groovy.resolve;

import com.intellij.groovy.testFramework.ResolveTest;
import com.intellij.psi.PsiMethod;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.JavaCodeInsightTestFixture;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightTestFixture;
import com.intellij.testFramework.fixtures.LightJavaProject;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.groovy.GrCucumberTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.testFramework.EdtTestUtil.runInEdtAndWait;
import static com.intellij.testFramework.fixtures.LightJavaFixturesKt.lightJavaCodeInsightFixture;
import static com.intellij.testFramework.fixtures.LightJavaFixturesKt.lightJavaProjectFixture;

@TestApplication
public class ResolveWorldTest implements ResolveTest {
  private static final TestFixture<LightJavaProject> project = lightJavaProjectFixture(GrCucumberTestUtil.DESCRIPTOR);
  private final TestFixture<LightJavaCodeInsightTestFixture> groovy = lightJavaCodeInsightFixture(project);

  @Override
  public @NotNull JavaCodeInsightTestFixture getFixture() {
    return groovy.get();
  }

  @BeforeEach
  public void addCustomWorld() {
    getFixture().addFileToProject("classes.groovy", """
class CustomWorld {
    String customMethod() {
        "foo"
    }
}
""");
  }

  private void resolveMethodTest(String text) {
    resolveTest("""
this.metaClass.mixin(cucumber.runtime.groovy.Hooks)
this.metaClass.mixin(cucumber.runtime.groovy.EN)

World {
    new CustomWorld()
}

""" + text + """
""", PsiMethod.class);
  }

  @Test
  public void resolveCustomWorldInHook() {
    resolveMethodTest("""
Before() {
    assert "foo" == custom<caret>Method()
}
""");
  }

  @Test
  public void custom_world_method_in_hook_in_inner_closure() {
    resolveMethodTest("""
Before() {
    [1,2,3].each { <caret>customMethod() }
}
""");
  }

  @Test
  public void resolveCustomWorldInStep() {
    resolveMethodTest("""
Given(~"I have entered (\\\\d+) into (.*) calculator") { int number, String ignore ->
    assert "foo" == custom<caret>Method()
}
""");
  }

  @Test
  public void custom_world_method_in_step_in_inner_closure() {
    resolveMethodTest("""
Given(~"I have entered (\\\\d+) into (.*) calculator") { int number, String ignore ->
    [1,2,3].each { <caret>customMethod() }
}
""");
  }

  @Test
  public void worldFromSameDirectoryFile() {
    getFixture().addFileToProject("otherSteps.groovy", """
this.metaClass.mixin(cucumber.runtime.groovy.Hooks)
this.metaClass.mixin(cucumber.runtime.groovy.EN)

World {
    new CustomWorld()
}
""");

    resolveTest("""
this.metaClass.mixin(cucumber.runtime.groovy.Hooks)
this.metaClass.mixin(cucumber.runtime.groovy.EN)

Given(~"I have entered (\\\\d+) into (.*) calculator") { int number, String ignore ->
    assert "foo" == custom<caret>Method()
}
""", PsiMethod.class);
  }
}
