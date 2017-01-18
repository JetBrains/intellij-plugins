package org.angularjs.codeInsight;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;

public class InjectionsFormattingTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "injections";
  }

  public void testStyles() {
    doTest("stylesFormatting.ts", "stylesFormatting_after.ts");
  }

  public void testTemplate() {
    doTest("templateFormatting.ts", "templateFormatting_after.ts");
  }

  private void doTest(@NotNull String before, @NotNull String expectedFile) {
    myFixture.configureByFile("angular2.js");
    PsiFile psiFile = myFixture.configureByFile(before);
    doReformat(psiFile);
    myFixture.checkResultByFile(expectedFile);
  }

  private void doReformat(@NotNull PsiElement file) {
    final CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(getProject());
    WriteCommandAction.runWriteCommandAction(getProject(), () -> {
      codeStyleManager.reformat(file);
    });
  }
}
