// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.refactoring;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSFixtureOptions;
import com.intellij.lang.javascript.refactoring.introduceVariable.JSInplaceIntroduceVariableFixture;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.Test;

import static com.intellij.lang.javascript.JSTestFixtures.jsProjectFixture;
import static com.intellij.lang.javascript.refactoring.introduceVariable.JSInplaceIntroduceVariableFixture.jsInplaceIntroduceVariableFixture;

@TestApplication
public class MxmlInplaceIntroduceVariableTest {
  private static final TestFixture<Project> projectFixture = jsProjectFixture();
  private final TestFixture<JSInplaceIntroduceVariableFixture> introduce = jsInplaceIntroduceVariableFixture(
    projectFixture,
    new JSFixtureOptions(FlexTestUtils.getTestDataPath("/refactoring/introduceVariable/"))
  );

  @Test
  public void testInitialOccurrenceSelected() {
    JSInplaceIntroduceVariableFixture introduceFixture = introduce.get();
    final String testName = introduceFixture.getTestName(false);
    introduceFixture.doTest("tmp", testName, ".mxml", testName);
  }

  @Test
  public void testInjectedAllOccurrences() {
    JSInplaceIntroduceVariableFixture introduceFixture = introduce.get();
    introduceFixture.doTest("created", introduceFixture.getTestName(false), ".mxml", true, null, null, introduceFixture.getTestName(false));
  }
}
