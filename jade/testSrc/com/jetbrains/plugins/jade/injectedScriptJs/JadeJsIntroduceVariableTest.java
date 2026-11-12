// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.injectedScriptJs;

import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.refactoring.introduceVariable.JSIntroduceVariableFixture;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.jetbrains.plugins.jade.JadeTestUtil;
import org.junit.jupiter.api.Test;

import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;
import static com.intellij.lang.javascript.refactoring.introduceVariable.JSIntroduceVariableFixture.jsIntroduceVariableFixture;

@TestApplication
public class JadeJsIntroduceVariableTest {
  private final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSIntroduceVariableFixture> js =
    jsIntroduceVariableFixture(projectFixture, new JSFixtureOptions(JadeTestUtil.getBaseTestDataPath() + "/injectedScriptJs/"));

  @Test
  public void testIntroduceVar() {
    js.get().doTest("a", false, ".jade");
  }
}
