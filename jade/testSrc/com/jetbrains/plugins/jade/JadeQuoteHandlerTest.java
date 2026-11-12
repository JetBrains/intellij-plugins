// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.openapi.project.Project;
import com.intellij.testFramework.EditorTestUtil;
import com.intellij.testFramework.EdtTestUtil;
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
public class JadeQuoteHandlerTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> fixture = sharedCodeInsightFixture(projectFixture);
  private String myTestName;

  @BeforeEach
  void setUp(TestInfo testInfo) {
    myTestName = PlatformTestUtil.getTestName(testInfo.getTestMethod().orElseThrow().getName(), true);
    fixture.get().setTestDataPath(JadeTestUtil.getBaseTestDataPath() + "/quoteHandler/");
  }

  @Test
  public void testMinusCode() {
    doTest();
  }

  @Test
  public void testScriptCode() {
    doTest();
  }

  @Test
  public void testAttribute1() {
    doTest();
  }

  @Test
  public void testAttribute2() {
    doTest();
  }

  @Test
  public void testAttribute3() {
    doTest();
  }

  private void doTest() {
    CodeInsightTestFixture myFixture = fixture.get();
    final String extension = ".jade";
    final String beforeFile = myTestName + "_unquoted" + extension;
    final String afterFile = myTestName + "_quoted" + extension;

    myFixture.configureByFile(beforeFile);
    EdtTestUtil.runInEdtAndWait(() -> EditorTestUtil.performTypingAction(myFixture.getEditor(), '"'));
    myFixture.checkResultByFile(afterFile);

    myFixture.configureByFile(afterFile);
    EdtTestUtil.runInEdtAndWait(() -> EditorTestUtil.performTypingAction(myFixture.getEditor(), EditorTestUtil.BACKSPACE_FAKE_CHAR));
    myFixture.checkResultByFile(beforeFile);
  }
}
