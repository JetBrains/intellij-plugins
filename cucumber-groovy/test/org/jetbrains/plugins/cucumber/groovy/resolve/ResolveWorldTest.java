// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.groovy.resolve;

import com.intellij.psi.PsiMethod;
import org.jetbrains.plugins.cucumber.groovy.GrCucumberLightTestCase;
import org.jetbrains.plugins.groovy.util.ResolveTest;
import org.junit.Before;
import org.junit.Test;

public class ResolveWorldTest extends GrCucumberLightTestCase implements ResolveTest {
  @Before
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
