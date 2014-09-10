package com.dmarcotte.handlebars.editor.actions;

import com.dmarcotte.handlebars.config.HbConfig;
import com.dmarcotte.handlebars.util.HbTestUtils;
import com.intellij.codeInsight.editorActions.moveUpDown.MoveStatementUpAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.testFramework.PlatformTestCase;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;


public class HbMoverTest extends LightPlatformCodeInsightFixtureTestCase {

  public void testMoveXmlWhenOpenHtmlAsHandlebars() throws Exception {
    boolean oldValue = HbConfig.shouldOpenHtmlAsHandlebars(getProject());

    HbConfig.setShouldOpenHtmlAsHandlebars(true, getProject());

    try {
      doTest("hbs");
    }
    finally {
      HbConfig.setShouldOpenHtmlAsHandlebars(oldValue, getProject());
    }
  }

  public void testMoveHbsTag() throws Exception {
    doTest("hbs");
  }


  public HbMoverTest() {
    PlatformTestCase.initPlatformLangPrefix();
  }

  private void doTest(String ext) throws Exception {
    final String baseName = getBasePath() + '/' + getTestName(true);
    final String fileName = baseName + "." + ext;

    @NonNls String afterFileName = baseName + "_after." + ext;
    EditorActionHandler handler = new MoveStatementUpAction().getHandler();
    performAction(fileName, handler, afterFileName);
  }

  private void performAction(final String fileName, final EditorActionHandler handler, final String afterFileName) throws Exception {
    myFixture.configureByFile(fileName);
    final boolean enabled = handler.isEnabled(myFixture.getEditor(), null);
    assertEquals("not enabled for " + afterFileName, new File(getTestDataPath(), afterFileName).exists(), enabled);
    if (enabled) {
      WriteCommandAction.runWriteCommandAction(null, new Runnable() {
        @Override
        public void run() {
          handler.execute(myFixture.getEditor(), null);
        }
      });
      myFixture.checkResultByFile(afterFileName);
    }
  }

  @Override
  protected String getBasePath() {
    return "mover";
  }

  @NotNull
  @Override
  protected String getTestDataPath() {
    return HbTestUtils.BASE_TEST_DATA_PATH;
  }
}
