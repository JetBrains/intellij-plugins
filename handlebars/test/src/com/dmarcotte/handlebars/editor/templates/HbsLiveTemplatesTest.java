package com.dmarcotte.handlebars.editor.templates;

import com.dmarcotte.handlebars.util.HbTestUtils;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.codeInsight.template.impl.actions.ListTemplatesAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.PlatformTestCase;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;


@SuppressWarnings("ConstantConditions")
public class HbsLiveTemplatesTest extends LightPlatformCodeInsightFixtureTestCase {

  @Override
  protected void setUp() throws Exception {
    PlatformTestCase.initPlatformLangPrefix();
    super.setUp();
  }

  @NotNull
  @Override
  protected String getTestDataPath() {
    return HbTestUtils.BASE_TEST_DATA_PATH + getBasePath();
  }

  @Override
  protected String getBasePath() {
    return FileUtil.toSystemDependentName("/liveTemplates/");
  }

  private void doTest() throws Exception {
    myFixture.configureByFiles(getTestName(false) + ".hbs");
    expandTemplate(myFixture.getEditor());
    myFixture.checkResultByFile(getTestName(false) + ".after.hbs");
  }

  public void testItar() throws Throwable {
    doTest();
  }


  private static void expandTemplate(final Editor editor) {
    WriteCommandAction.runWriteCommandAction(null, new Runnable() {
      @Override
      public void run() {
        new ListTemplatesAction().actionPerformedImpl(editor.getProject(), editor);
        ((LookupImpl)LookupManager.getActiveLookup(editor)).finishLookup(Lookup.NORMAL_SELECT_CHAR);
      }
    });
  }
}
