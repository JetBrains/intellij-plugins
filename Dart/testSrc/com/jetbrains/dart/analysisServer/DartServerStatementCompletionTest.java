// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.dart.analysisServer;

import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.jetbrains.annotations.NotNull;

public class DartServerStatementCompletionTest extends CodeInsightFixtureTestCase {
  @Override
  public void setUp() throws Exception {
    super.setUp();
    DartTestUtils.configureDartSdk(myModule, myFixture.getTestRootDisposable(), true);
    DartAnalysisServerService.getInstance(getProject()).serverReadyForRequest();
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
  }

  public void doTest(@NotNull final String before, @NotNull final String after) {
    myFixture.configureByText("complete.dart", before);
    // Apparently, highlighting causes the editor and analysis server to be in sync with the file system.
    myFixture.doHighlighting(); // Basically, this is magic to de-flake tests.
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_COMPLETE_STATEMENT);
    myFixture.checkResult(after);
  }

  public void testIfBlock() {
    doTest(
      """
        main() {
          if (true)<caret>
        }""",
      """
        main() {
          if (true) {
            <caret>
          }
        }"""
    );
  }

  public void testWhileBlock() {
    doTest(
      """
        main() {
          while (true)<caret>
        }""",
      """
        main() {
          while (true) {
            <caret>
          }
        }"""
    );
  }
}
