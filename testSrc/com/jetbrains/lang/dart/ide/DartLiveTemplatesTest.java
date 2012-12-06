package com.jetbrains.lang.dart.ide;

import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.codeInsight.template.impl.actions.ListTemplatesAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

/**
 * @author: Fedor.Korotkov
 */
public class DartLiveTemplatesTest extends LightCodeInsightFixtureTestCase {
  @Override
  protected String getBasePath() {
    return FileUtil.toSystemDependentName("/web-ide/WebStorm/Dart/testData/liveTemplates/");
  }

  public static void expandTemplate(final Editor editor) {
    final Project project = editor.getProject();
    assertNotNull(project);
    new ListTemplatesAction().actionPerformedImpl(project, editor);
    final LookupImpl lookup = (LookupImpl)LookupManager.getActiveLookup(editor);
    assertNotNull(lookup);
    lookup.finishLookup(Lookup.NORMAL_SELECT_CHAR);
  }

  private void doTest() throws Exception {
    doTest(getTestName(false) + ".dart");
  }

  private void doTest(String... files) throws Exception {
    myFixture.configureByFiles(files);
    expandTemplate(myFixture.getEditor());
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        CodeStyleManager.getInstance(myFixture.getProject()).reformat(myFixture.getFile());
      }
    });
    myFixture.getEditor().getSelectionModel().removeSelection();
    myFixture.checkResultByFile(getTestName(false) + ".after.dart");
  }

  public void testItar1() throws Throwable {
    doTest();
  }

  public void testItar2() throws Throwable {
    doTest();
  }

  public void testIter() throws Throwable {
    doTest();
  }
}
