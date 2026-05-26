package org.intellij.plugins.postcss.parser;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.testFramework.ParsingTestCase;
import com.intellij.testFramework.UsefulTestCase;
import org.intellij.plugins.postcss.PostCssFixtureTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class PostCssIncrementalParserTest extends PostCssFixtureTestCase {

  public void testDeclarationBlockWithBraces() {
    doTest(":root {\n    --mainColor: red;\n}");
  }

  public void testDeclarationBlockWithoutBraces() {
    doTest("--mainColor: red;");
  }

  public void testDeclarationBlockWithNonPairBraces() {
    doTest(":root {\n    --mainColor: red;");
  }

  private void doTest(@NotNull final String text) {
    myFixture.configureByFile(getTestName(true) + ".pcss");
    type(text);
    PsiFile psiFile = myFixture.getFile();
    ParsingTestCase.ensureParsed(psiFile);
    String actual = DebugUtil.psiToString(psiFile, true, false);
    UsefulTestCase.assertSameLinesWithFile(getTestDataPath() + File.separatorChar + getTestName(true) + ".txt", actual);
  }

  private void type(@NotNull final String insertString) {
    WriteCommandAction.runWriteCommandAction(getProject(), () -> {
      Document document = myFixture.getEditor().getDocument();
      document.insertString(myFixture.getCaretOffset(), insertString);
      PsiDocumentManager.getInstance(getProject()).commitDocument(document);
    });
  }

  @NotNull
  @Override
  protected String getTestDataSubdir() {
    return "incremental";
  }
}