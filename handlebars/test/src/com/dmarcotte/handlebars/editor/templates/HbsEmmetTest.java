package com.dmarcotte.handlebars.editor.templates;

import com.dmarcotte.handlebars.file.HbFileType;
import com.dmarcotte.handlebars.psi.HbPsiFile;
import com.dmarcotte.handlebars.util.HbTestUtils;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateSettings;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.PlatformTestCase;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

public class HbsEmmetTest extends LightPlatformCodeInsightFixtureTestCase {

  public void testSimpleTags() {
    myFixture.configureByText(HbFileType.INSTANCE, "div>span<caret>");
    TemplateManager.getInstance(getProject()).startTemplate(myFixture.getEditor(), TemplateSettings.TAB_CHAR);
    myFixture.checkResult("<div><span></span></div>");
  }

  public void testSimpleTagsWithHtmlSubstitutor() {
    HbTestUtils.setOpenHtmlAsHandlebars(true, getProject(), getTestRootDisposable());
    final PsiFile file = myFixture.configureByText("test.html", "div>span<caret>");
    assertInstanceOf(file, HbPsiFile.class);
    TemplateManager.getInstance(getProject()).startTemplate(myFixture.getEditor(), TemplateSettings.TAB_CHAR);
    myFixture.checkResult("<div><span></span></div>");
  }
}
