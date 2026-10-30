// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.refactoring;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.refactoring.introduceVariable.JSIntroduceVariableFixture;
import com.intellij.openapi.project.Project;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.Test;

import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;
import static com.intellij.lang.javascript.refactoring.introduceVariable.JSIntroduceVariableFixture.jsIntroduceVariableFixture;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestApplication
public class MxmlIntroduceVariableTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSIntroduceVariableFixture> js =
    jsIntroduceVariableFixture(projectFixture, new JSFixtureOptions(FlexTestUtils.getTestDataPath("/refactoring/introduceVariable/")));

  @Test
  public void testKeepCData() {
    js.get().doTest("created", true, ".mxml");
  }

  @Test
  public void testNoIntroduceInBinding() {
    assertThrows(CommonRefactoringUtil.RefactoringErrorHintException.class, () -> js.get().doTest("created", true, ".mxml"));
  }
}
