package org.angularjs.codeInsight;

import com.intellij.codeInsight.documentation.DocumentationManager;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.presentation.java.SymbolPresentationUtil;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.util.containers.ContainerUtil;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightProjectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.testNameFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Dennis.Ushakov
 */
@TestApplication
public class DocumentationTest {
  private final TestFixture<Project> projectFixture = codeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsight = codeInsightFixture(projectFixture);
  private final TestFixture<String> testName = testNameFixture();

  @BeforeEach
  void setUpTestDataPath() {
    codeInsight.get().setTestDataPath(AngularTestUtil.getBaseTestDataPath(getClass()) + "documentation");
  }

  @Test
  public void testDocumentation() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("standard.html", "angular.js");

    ReadAction.run(() -> {
      Editor editor = myFixture.getEditor();
      PsiFile file = myFixture.getFile();
      PsiElement originalElement = file.findElementAt(editor.getCaretModel().getOffset());
      assertNotNull(originalElement);

      final PsiElement targetElement = DocumentationManager.getInstance(myFixture.getProject()).findTargetElement(editor, file);
      assertNotNull(targetElement);
      assertEquals("ngController", SymbolPresentationUtil.getSymbolPresentableText(targetElement)); // WEB-16957
      assertDocumentation(targetElement, originalElement);
    });
  }

  @Test
  public void testDocumentationInLookup() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles("standardCompletion.html", "angular.js");

    PsiElement context = ReadAction.compute(() -> myFixture.getFile().findElementAt(myFixture.getEditor().getCaretModel().getOffset() - 1));
    assertNotNull(context);

    myFixture.completeBasic();
    LookupElement[] elements = myFixture.getLookupElements();
    assertNotNull(elements);
    LookupElement lookupElement = ContainerUtil.find(elements, element -> element.getAllLookupStrings().contains("ng-controller"));
    assertNotNull(lookupElement);

    ReadAction.run(() -> {
      DocumentationProvider documentationProvider = DocumentationManager.getProviderFromElement(myFixture.getFile());
      PsiElement docElement = documentationProvider.getDocumentationElementForLookupItem(PsiManager.getInstance(myFixture.getProject()), lookupElement.getObject(), context);
      assertNotNull(docElement);
      assertDocumentation(docElement, context);
    });
  }

  private void assertDocumentation(@NotNull PsiElement docElement, @NotNull PsiElement context) {
    DocumentationProvider documentationProvider = DocumentationManager.getProviderFromElement(docElement);
    String inlineDoc = documentationProvider.generateDoc(docElement, context);
    assertNotNull(inlineDoc, "inline help is null");
    List<String> urlFor = documentationProvider.getUrlFor(docElement, context);
    assertNotNull(urlFor, "external help is null");
    PlatformTestUtil.assertSameLinesWithFile(codeInsight.get().getTestDataPath() + "/" + testName.get() + ".txt", inlineDoc + "\n---\n" + StringUtil.join(urlFor, "\n"));
  }
}
