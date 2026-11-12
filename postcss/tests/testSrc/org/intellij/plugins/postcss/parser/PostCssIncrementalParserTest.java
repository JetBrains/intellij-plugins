package org.intellij.plugins.postcss.parser;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.testFramework.ParsingTestUtil;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.intellij.plugins.postcss.PostCssFixture;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.File;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;
import static org.intellij.plugins.postcss.PostCssFixtureKt.postCssFixture;

@TestApplication
@TestDataPath("$PROJECT_ROOT/contrib/postcss/tests/testData/parser/incremental")
public class PostCssIncrementalParserTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<PostCssFixture> postCss = postCssFixture(projectFixture);

  @Test
  public void testDeclarationBlockWithBraces() {
    doTest(":root {\n    --mainColor: red;\n}");
  }

  @Test
  public void testDeclarationBlockWithoutBraces() {
    doTest("--mainColor: red;");
  }

  @Test
  public void testDeclarationBlockWithNonPairBraces() {
    doTest(":root {\n    --mainColor: red;");
  }

  private void doTest(@NotNull final String text) {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    myFixture.configureByFile(postCss.get().getTestName() + ".pcss");
    type(text);
    PsiFile psiFile = myFixture.getFile();
    String actual = ReadAction.computeBlocking(() -> {
      ParsingTestUtil.ensureParsed(psiFile);
      return DebugUtil.psiToString(psiFile, true, false);
    });
    PlatformTestUtil.assertSameLinesWithFile(myFixture.getTestDataPath() + File.separatorChar + postCss.get().getTestName() + ".txt", actual);
  }

  private void type(@NotNull final String insertString) {
    CodeInsightTestFixture myFixture = postCss.get().getFixture();
    WriteCommandAction.runWriteCommandAction(myFixture.getProject(), () -> {
      Document document = myFixture.getEditor().getDocument();
      document.insertString(myFixture.getCaretOffset(), insertString);
      PsiDocumentManager.getInstance(myFixture.getProject()).commitDocument(document);
    });
  }
}