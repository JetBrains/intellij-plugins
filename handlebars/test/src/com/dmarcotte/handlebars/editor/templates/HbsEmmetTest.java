package com.dmarcotte.handlebars.editor.templates;

import com.dmarcotte.handlebars.file.HbFileType;
import com.dmarcotte.handlebars.psi.HbPsiFile;
import com.dmarcotte.handlebars.util.HbTestUtils;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateSettings;
import com.intellij.openapi.application.impl.NonBlockingReadActionImpl;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.util.ui.UIUtil;

public class HbsEmmetTest extends BasePlatformTestCase {

  public void testSimpleTags() {
    myFixture.configureByText(HbFileType.INSTANCE, "div>span<caret>");
    WriteCommandAction.runWriteCommandAction(
      getProject(),
      () -> {
        TemplateManager.getInstance(getProject()).startTemplate(myFixture.getEditor(), TemplateSettings.TAB_CHAR);
      });

    checkResult();
  }

  public void testSimpleTagsWithHtmlSubstitutor() {
    HbTestUtils.setOpenHtmlAsHandlebars(true, getProject(), myFixture.getTestRootDisposable());
    final PsiFile file = myFixture.configureByText("test.html", "div>span<caret>");
    assertInstanceOf(file, HbPsiFile.class);
    WriteCommandAction.runWriteCommandAction(
      getProject(),
      () -> {
        TemplateManager.getInstance(getProject()).startTemplate(myFixture.getEditor(), TemplateSettings.TAB_CHAR);
      });

    checkResult();
  }

  private void checkResult() {
    NonBlockingReadActionImpl.waitForAsyncTaskCompletion();
    UIUtil.dispatchAllInvocationEvents();

    myFixture.checkResult("<div><span></span></div>");
  }
}
