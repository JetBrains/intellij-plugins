package org.angularjs.codeInsight;

import com.intellij.codeInsight.documentation.DocumentationManager;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.presentation.java.SymbolPresentationUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.util.containers.ContainerUtil;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class DocumentationTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "documentation";
  }

  public void testDocumentation() {
    myFixture.configureByFiles("standard.html", "angular.js");

    Editor editor = myFixture.getEditor();
    PsiFile file = myFixture.getFile();
    PsiElement originalElement = file.findElementAt(editor.getCaretModel().getOffset());
    assertNotNull(originalElement);

    final PsiElement targetElement = DocumentationManager.getInstance(getProject()).findTargetElement(editor, file);
    assertNotNull(targetElement);
    assertEquals("ng-controller", SymbolPresentationUtil.getSymbolPresentableText(targetElement)); // WEB-16957
    assertDocumentation(targetElement, originalElement);
  }

  public void testDocumentationInLookup() {
    myFixture.configureByFiles("standardCompletion.html", "angular.js");

    PsiElement context = myFixture.getFile().findElementAt(myFixture.getEditor().getCaretModel().getOffset() - 1);
    assertNotNull(context);

    myFixture.completeBasic();
    LookupElement[] elements = myFixture.getLookupElements();
    assertNotNull(elements);
    LookupElement lookupElement = ContainerUtil.find(elements, element -> element.getAllLookupStrings().contains("ng-controller"));
    assertNotNull(lookupElement);

    DocumentationProvider documentationProvider = DocumentationManager.getProviderFromElement(myFixture.getFile());
    PsiElement docElement = documentationProvider.getDocumentationElementForLookupItem(getPsiManager(), lookupElement.getObject(), context);
    assertNotNull(docElement);
    assertDocumentation(docElement, context);
  }

  private void assertDocumentation(@NotNull PsiElement docElement, @NotNull PsiElement context) {
    DocumentationProvider documentationProvider = DocumentationManager.getProviderFromElement(docElement);
    String inlineDoc = documentationProvider.generateDoc(docElement, context);
    assertNotNull("inline help is null", inlineDoc);
    List<String> urlFor = documentationProvider.getUrlFor(docElement, context);
    assertNotNull("external help is null", urlFor);
    assertSameLinesWithFile(getTestDataPath() + "/" + getTestName(true) + ".txt", inlineDoc + "\n---\n" + StringUtil.join(urlFor, "\n"));
  }
}
