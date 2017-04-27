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
    DartAnalysisServerService.getInstance(getProject()).serverReadyForRequest(getProject());
    ((CodeInsightTestFixtureImpl)myFixture).canChangeDocumentDuringHighlighting(true);
  }

  public void doTest(@NotNull final String before, @NotNull final String after) {
    myFixture.configureByText("complete.dart", before);
    // Apparently, highlighting causes the editor and analysis server to be in sync with the file system.
    myFixture.doHighlighting(); // Basically, this is magic to de-flake tests.
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_COMPLETE_STATEMENT);
    myFixture.checkResult(after);
  }

  public void testIfBlock() throws Exception {
    doTest(
      "main() {\n" +
      "  if (true)<caret>\n" +
      "}",
      "main() {\n" +
      "  if (true) {\n" +
      "    <caret>\n" +
      "  }\n" +
      "}"
    );
  }

  public void testWhileBlock() throws Exception {
    doTest(
      "main() {\n" +
      "  while (true)<caret>\n" +
      "}",
      "main() {\n" +
      "  while (true) {\n" +
      "    <caret>\n" +
      "  }\n" +
      "}"
    );
  }
}
