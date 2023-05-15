// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.formatting;

import com.intellij.application.options.CodeStyle;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.xml.HtmlCodeStyleSettings;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;

public class FormattingTest extends Angular2CodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass());
  }

  public void testStyles() {
    doTest("stylesFormatting_after.ts", "stylesFormatting.ts");
  }

  public void testTemplate() {
    doTest("templateFormatting_after.ts", "templateFormatting.ts");
  }

  public void testAttrs() {
    HtmlCodeStyleSettings htmlSettings = getSettings().getCustomSettings(HtmlCodeStyleSettings.class);
    htmlSettings.HTML_ATTRIBUTE_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS;
    htmlSettings.HTML_SPACE_AROUND_EQUALITY_IN_ATTRIBUTE = true;
    doTest("attrFormatting_after.html", "attrFormatting.html", "attrFormatting.ts");
  }

  public void testInnerAttrs() {
    HtmlCodeStyleSettings htmlSettings = getSettings().getCustomSettings(HtmlCodeStyleSettings.class);
    htmlSettings.HTML_ATTRIBUTE_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS;
    htmlSettings.HTML_SPACE_AROUND_EQUALITY_IN_ATTRIBUTE = true;
    doTest("innerAttrFormatting_after.ts", "innerAttrFormatting.ts");
  }

  public void testNoKeepLineBreaks() {
    HtmlCodeStyleSettings htmlSettings = getSettings().getCustomSettings(HtmlCodeStyleSettings.class);
    htmlSettings.HTML_ATTRIBUTE_WRAP = CommonCodeStyleSettings.WRAP_AS_NEEDED;
    htmlSettings.HTML_SPACE_AROUND_EQUALITY_IN_ATTRIBUTE = false;
    htmlSettings.HTML_KEEP_LINE_BREAKS = false;
    htmlSettings.HTML_KEEP_BLANK_LINES = 0;
    doTest("noKeepLineBreakFormatting_after.html", "noKeepLineBreakFormatting.html");
  }

  public void testAttributeTyping() {
    myFixture.configureByFiles("attrTyping.html", "package.json");
    myFixture.type("\ntest2\n[test]=\"\"\n[(banana)]=\"\"\nother\n");
    myFixture.checkResultByFile("attrTyping_after.html");
  }

  private void doTest(@NotNull String expectedFile, String @NotNull ... before) {
    myFixture.configureByFile("package.json");
    PsiFile psiFile = myFixture.configureByFiles(before)[0];
    doReformat(psiFile);
    myFixture.checkResultByFile(expectedFile);
  }

  private void doReformat(@NotNull PsiElement file) {
    final CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(getProject());
    WriteCommandAction.runWriteCommandAction(getProject(), () -> {
      codeStyleManager.reformat(file);
    });
  }

  protected CodeStyleSettings getSettings() {
    return CodeStyle.getSettings(getProject());
  }
}
