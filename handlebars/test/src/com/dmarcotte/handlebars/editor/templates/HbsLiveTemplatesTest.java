package com.dmarcotte.handlebars.editor.templates;

import com.dmarcotte.handlebars.util.HbTestUtils;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;


public class HbsLiveTemplatesTest extends LightPlatformCodeInsightFixtureTestCase {

  @NotNull
  @Override
  protected String getTestDataPath() {
    return HbTestUtils.BASE_TEST_DATA_PATH + getBasePath();
  }

  @Override
  protected String getBasePath() {
    return FileUtil.toSystemDependentName("/liveTemplates/");
  }

  private void doTest() {
    myFixture.configureByFiles(getTestName(false) + ".hbs");
    expandTemplate();
    myFixture.checkResultByFile(getTestName(false) + ".after.hbs");
  }

  public void testItar() {
    doTest();
  }

  private void expandTemplate() {
    EditorAction action = (EditorAction)ActionManager.getInstance().getAction(IdeActions.ACTION_EXPAND_LIVE_TEMPLATE_BY_TAB);
    action.actionPerformed(myFixture.getEditor(), DataManager.getInstance().getDataContext());
  }
}
