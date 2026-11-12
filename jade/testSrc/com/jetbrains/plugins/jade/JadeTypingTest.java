// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.idea.IJIgnore;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;

@TestApplication
public class JadeTypingTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> fixture = sharedCodeInsightFixture(projectFixture);
  private String myTestName;

  @BeforeEach
  void setUp(TestInfo testInfo) {
    myTestName = PlatformTestUtil.getTestName(testInfo.getTestMethod().orElseThrow().getName(), true);
    fixture.get().setTestDataPath(JadeTestUtil.getBaseTestDataPath() + "/typing/");
  }

  @Test
  public void testEnterDoctype() {
    doTest("\n");
  }

  @Test
  public void testEnterTag() {
    doTest("\n");
  }

  @Test
  public void testEnterComment() {
    doTest("\n");
  }

  @Test
  @IJIgnore(issue = "AT-4013")
  public void testEnterEmbeddedJS() {
    doTest("\n");
  }

  @Test
  @IJIgnore(issue = "AT-4013")
  public void testEnterEmbeddedJSToplevel() {
    doTest("\n");
  }

  @Test
  public void testBackspace() {
    doTest("\b\b");
  }

  private void doTest(String toType) {
    CodeInsightTestFixture myFixture = fixture.get();
    myFixture.configureByFile('/' + myTestName + ".jade");
    myFixture.type(toType);
    myFixture.checkResultByFile('/' + myTestName + "_after.jade");
  }

}
