// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.dart.analysisServer;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DartServerIntentionsTest extends CodeInsightFixtureTestCase {
  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, myFixture.getTestRootDisposable(), true);
    DartAnalysisServerService.getInstance(getProject()).serverReadyForRequest();
    myFixture.setTestDataPath(DartTestUtils.BASE_TEST_DATA_PATH + getBasePath());
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
  }

  @Override
  protected String getBasePath() {
    return "/analysisServer/intentions";
  }

  private void doTest(@NotNull final String intentionName) {
    myFixture.configureByFile(getTestName(false) + ".dart");
    final IntentionAction intention = myFixture.findSingleIntention(intentionName);

    ApplicationManager.getApplication().runWriteAction(() -> intention.invoke(getProject(), getEditor(), getFile()));

    myFixture.checkResultByFile(getTestName(false) + ".after.dart");
  }

  public void testIntroduceVariableNoSelection() {
    doTest("Assign value to new local variable");
  }

  public void testSurroundWithTryCatch() {
    // TODO selection in the 'after' file is incorrect
    doTest("Surround with 'try-catch'");
  }

  public void testIntentionsOrder() {
    myFixture.configureByText("foo.dart", "void f() {\n" +
                                          "  <selection><caret>var x = 3;</selection>\n" +
                                          "}\n");
    final List<String> intentions = ContainerUtil.map(myFixture.getAvailableIntentions(), intention -> intention.getText());
    assertOrderedEquals(intentions,
                        "Surround with block",
                        "Edit intention settings",
                        "Disable 'Quick assist powered by the Dart Analysis Server'",
                        "Surround with 'if'",
                        "Edit intention settings",
                        "Disable 'Quick assist powered by the Dart Analysis Server'",
                        "Surround with 'while'",
                        "Edit intention settings",
                        "Disable 'Quick assist powered by the Dart Analysis Server'",
                        "Surround with 'for-in'",
                        "Edit intention settings",
                        "Disable 'Quick assist powered by the Dart Analysis Server'",
                        "Surround with 'for'",
                        "Edit intention settings",
                        "Disable 'Quick assist powered by the Dart Analysis Server'",
                        "Surround with 'do-while'",
                        "Edit intention settings",
                        "Disable 'Quick assist powered by the Dart Analysis Server'",
                        "Surround with 'try-catch'",
                        "Edit intention settings",
                        "Disable 'Quick assist powered by the Dart Analysis Server'",
                        "Surround with 'try-finally'",
                        "Edit intention settings",
                        "Disable 'Quick assist powered by the Dart Analysis Server'",
                        "Adjust code style settings");
  }
}
