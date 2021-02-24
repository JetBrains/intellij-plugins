package org.angular2.codeInsight;

import com.intellij.codeInsight.documentation.DocumentationManager;
import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.angular2.Angular2CodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;

public class Angular2DocumentationTest extends Angular2CodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "documentation";
  }

  public void testTagName() {
    doTest();
  }

  public void testSimpleInput() {
    doTest();
  }

  public void testSimpleInputBinding() {
    doTest();
  }

  public void testSimpleOutputBinding() {
    doTest();
  }

  public void testSimpleBananaBox() {
    doTest();
  }

  public void testDirectiveWithMatchingInput() {
    doTest();
  }

  public void testDirectiveWithoutMatchingInput() {
    doTest();
  }

  public void testGlobalAttribute() {
    doTest();
  }

  public void testFieldWithoutDocs() {
    doTest();
  }

  public void testFieldWithDocsPrivate() {
    doTest();
  }

  private void doTest() {
    doTest("html", true);
  }

  private void doTest(@NotNull String extension, boolean shouldHaveDoc) {
    myFixture.configureByFiles(getTestName(true) + "." + extension,
                               "package.json", "deps/list-item.component.ts", "deps/ng_for_of.ts", "deps/ng_if.ts", "deps/dir.ts", "deps/ng_plural.ts");

    Editor editor = myFixture.getEditor();
    PsiFile file = myFixture.getFile();
    PsiElement originalElement = file.findElementAt(editor.getCaretModel().getOffset());
    assertNotNull(originalElement);

    assertDocumentation(DocumentationManager.getInstance(getProject()).findTargetElement(editor, file), originalElement, shouldHaveDoc);
  }

  private void assertDocumentation(@NotNull PsiElement docElement, @NotNull PsiElement context, boolean shouldHaveDoc) {
    DocumentationProvider documentationProvider = DocumentationManager.getProviderFromElement(context);

    String inlineDoc = documentationProvider.generateDoc(docElement, context);
    if (shouldHaveDoc) {
      assertNotNull("inline help is null", inlineDoc);
    }
    else {
      assertNull("inline help is not null", inlineDoc);
    }

    if (shouldHaveDoc) {
      assertSameLinesWithFile(getTestDataPath() + "/" + getTestName(true) + ".txt", inlineDoc);
    }
  }
}
