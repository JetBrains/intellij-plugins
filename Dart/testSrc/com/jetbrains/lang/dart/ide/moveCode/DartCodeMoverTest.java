package com.jetbrains.lang.dart.ide.moveCode;

import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;

abstract public class DartCodeMoverTest extends DartCodeInsightFixtureTestCase {

  protected void doTest() {
    final String testName = getTestName(false);
    myFixture.configureByFile(testName + ".dart");
    myFixture.performEditorAction(IdeActions.ACTION_MOVE_STATEMENT_UP_ACTION);
    myFixture.checkResultByFile(testName + "_afterUp.dart", true);

    FileDocumentManager.getInstance().reloadFromDisk(myFixture.getDocument(myFixture.getFile()));
    myFixture.configureByFile(testName + ".dart");
    myFixture.performEditorAction(IdeActions.ACTION_MOVE_STATEMENT_DOWN_ACTION);
    myFixture.checkResultByFile(testName + "_afterDown.dart", true);
  }
}
