package com.dmarcotte.handlebars.editor.actions;

import com.dmarcotte.handlebars.util.HbTestUtils;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

public class HbMoverTest extends BasePlatformTestCase {
  public void testMoveHtmlTextWhenOpenHtmlAsHandlebars() {
    HbTestUtils.setOpenHtmlAsHandlebars(true, getProject(), myFixture.getTestRootDisposable());
    doTest("hbs");
  }

  public void testMoveHbsTag() {
    doTest("hbs");
  }

  private void doTest(@NotNull String ext) {
    myFixture.configureByFile(getTestName(false) + "." + ext);
    myFixture.performEditorAction(IdeActions.ACTION_MOVE_STATEMENT_UP_ACTION);
    myFixture.checkResultByFile(getTestName(false) + "_after." + ext);
  }
  
  @NotNull
  @Override
  protected String getTestDataPath() {
    return HbTestUtils.BASE_TEST_DATA_PATH + "/mover";
  }
}
