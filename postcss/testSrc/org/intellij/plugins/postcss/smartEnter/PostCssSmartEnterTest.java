package org.intellij.plugins.postcss.smartEnter;

import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.util.IncorrectOperationException;
import org.intellij.plugins.postcss.PostCssFixtureTestCase;

public class PostCssSmartEnterTest extends PostCssFixtureTestCase {

  public void testCompleteCustomSelectorWithSemicolon() {
    doTest();
  }

  public void testCompleteCustomMediaWithSemicolon() {
    doTest();
  }

  private void doTest() throws IncorrectOperationException {
    myFixture.configureByFile(getTestName(true) + "_before.pcss");
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_COMPLETE_STATEMENT);
    myFixture.checkResultByFile(getTestName(true) + "_after.pcss");
  }
}