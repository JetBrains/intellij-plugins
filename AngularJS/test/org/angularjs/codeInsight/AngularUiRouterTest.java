package org.angularjs.codeInsight;

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase;
import com.intellij.lang.javascript.psi.impl.JSOffsetBasedImplicitElement;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightProjectFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@TestApplication
public class AngularUiRouterTest {
  private final TestFixture<Project> projectFixture = codeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsight = codeInsightFixture(projectFixture);

  @BeforeEach
  void setUpTestDataPath() {
    codeInsight.get().setTestDataPath(AngularTestUtil.getBaseTestDataPath(getClass()) + "uiRouter");
  }

  @Test
  public void testSimpleViewCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    final List<String> variants = myFixture.getCompletionVariants("simpleView.completion.js", "one.html", "two.html", "angular.js");
    assertEquals("menuTip", variants.get(0));
  }

  @Test
  public void testPartialTypedViewCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    final List<String> variants = myFixture.getCompletionVariants("partialTypedView.completion.js", "one.html", "two.html", "angular.js");
    assertEquals("menuTip", variants.get(0));
  }

  @Test
  public void testPartialTypedViewNavigation() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    final PsiFile[] files = myFixture.configureByFiles("partialTypedView.navigation.js", "one.html", "two.html", "angular.js");
    myFixture.doHighlighting();

    testNavigationToMenuTip(files[0]);
  }

  @Test
  public void testNoNavigationFromViews() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    //when there is not a views definition
    final PsiFile[] files = myFixture.configureByFiles("noNavigationFromViews.js", "angular.js");
    myFixture.doHighlighting();

    ReadAction.run(() -> {
      final PsiElement another = getReferenceByTextAround(files[0], "another").resolve();
      assertTrue(another instanceof JSProperty);
      assertEquals("another", ((JSProperty) another).getName());
      final PsiElement boring = getReferenceByTextAround(files[0], "boring").resolve();
      assertTrue(boring instanceof JSProperty);
      assertEquals("boring", ((JSProperty) boring).getName());
    });
  }

  private void testNavigationToMenuTip(PsiFile file) {
    ReadAction.run(() -> {
      final String str = "'menuTip'";
      final PsiReference reference = getReferenceByTextAround(file, str);
      assertEquals(StringUtil.unquoteString(str), reference.getCanonicalText());

      final PsiElement resolve = reference.resolve();
      assertNotNull(resolve);
      assertEquals("one.html", resolve.getContainingFile().getName());
      assertEquals(StringUtil.unquoteString(str), ((JSPsiNamedElementBase) resolve).getName());
    });
  }

  @Test
  public void testInnerPropertyControllerAs() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    final List<String> variants = myFixture.getCompletionVariants("innerPropertyControllerAs.completion.js", "one.html", "two.html", "angular.js");
    assertEquals("testMe", variants.get(0));
  }

  @Test
  public void testControllerRedefinitionSyntaxOutside() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    final List<String> variants = myFixture.getCompletionVariants("controllerRedefinitionSyntaxOutside.completion.js", "one.html", "two.html", "angular.js");
    assertTrue(variants.contains("testMe"));
    assertTrue(variants.contains("something"));
  }

  @Test
  public void testControllerRedefinitionSyntaxNavigation() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    final String mainFile = "controllerRedefinitionSyntax.navigation.js";
    final PsiFile[] files = myFixture.configureByFiles(mainFile, "one.html", "two.html", "angular.js");
    myFixture.doHighlighting();
    final String str = "'testMe as something'";
    ReadAction.run(() -> {
      final PsiElement element = getElement(files[0], str);
      assertEquals(str, element.getText());
      assertTrue(element.getParent() instanceof JSLiteralExpression);
      assertTrue(element.getParent().getParent() instanceof JSProperty);

      final PsiReference[] references = element.getParent().getReferences();
      assertEquals(1, references.length);
      assertEquals("testMe", references[0].getCanonicalText());

      final PsiElement resolve = references[0].resolve();
      assertNotNull(resolve);
      assertEquals(mainFile, resolve.getContainingFile().getName());
      assertEquals("testMe", ((JSPsiNamedElementBase) resolve).getName());
    });
  }

  @Test
  public void testNavigationToNamedView() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    final PsiFile[] files = myFixture.configureByFiles("appWithViews.navigation.js", "one.html", "two.html", "angular.js");
    myFixture.doHighlighting();

    testNavigationToMenuTip(files[0]);
  }

  @NotNull
  private PsiReference getReferenceByTextAround(PsiFile file, String str) {
    return ReadAction.compute(() -> {
      final PsiElement element = getElement(file, str);
      assertTrue(element.getParent() instanceof JSProperty);
      PsiReference reference = element.getParent().getReference();
      assertNotNull(reference);
      return reference;
    });
  }

  @NotNull
  private PsiElement getElement(PsiFile file, String str) {
    return ReadAction.compute(() -> {
      CodeInsightTestFixture myFixture = codeInsight.get();
      final int idx = myFixture.getEditor().getDocument().getText().indexOf(str);
      assertTrue(idx > 0);
      final PsiElement element = file.findElementAt(idx);
      assertNotNull(element);
      return element;
    });
  }

  @Test
  public void testNavigationToDefaultView() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    final PsiFile[] files = myFixture.configureByFiles("appWithViews.navigation.js", "one.html", "two.html", "angular.js");
    myFixture.doHighlighting();

    emptyViewNavigatesToFilesDefaultView(files[0], "'one.html'");
    emptyViewNavigatesToFilesDefaultView(files[0], "'two.html'");
  }

  private void emptyViewNavigatesToFilesDefaultView(PsiFile file, String str) {
    ReadAction.run(() -> {
      final PsiElement inObj = getElement(file, str);
      assertTrue(inObj.getParent() instanceof JSLiteralExpression);
      assertTrue(inObj.getParent().getParent() instanceof JSProperty);
      final JSProperty templateUrl = (JSProperty)inObj.getParent().getParent();
      assertEquals("templateUrl", templateUrl.getName());
      final PsiElement parentProperty = templateUrl.getParent().getParent();
      assertTrue(parentProperty instanceof JSProperty);
      final PsiReference reference = parentProperty.getReference();
      assertNotNull(reference);
      assertEquals("", reference.getCanonicalText());

      final PsiElement resolve = reference.resolve();
      assertNotNull(resolve);
      assertEquals(StringUtil.unquoteString(str), resolve.getContainingFile().getName());
      assertEquals("", ((JSPsiNamedElementBase) resolve).getName());

      final PsiElement element = ((JSOffsetBasedImplicitElement)resolve).getElementAtOffset();
      assertEquals("ui-view", element.getText());
    });
  }

  // states
  @Test
  public void testStatesCompletion() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    final List<String> variants = myFixture.getCompletionVariants("stateReferences.completion.html", "appStates.js", "angular.js");
    assertTrue(variants.contains("one"));
    assertTrue(variants.contains("two"));
    assertTrue(variants.contains("two.words"));
  }

  @Test
  public void testStatesNavigation() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    final PsiFile[] files = myFixture.configureByFiles("stateReferences.navigation.html", "appStates.js", "angular.js");
    myFixture.doHighlighting();
    checkNavigation(files[0], "one", null, "appStates.js");
    checkNavigation(files[0], "two", null, "appStates.js");
    checkNavigation(files[0], "two.words", null, "appStates.js");
    checkNavigation(files[0], ".words", "two.words", "appStates.js");
  }

  @Test
  public void testStatesNavigationForStatesWithNameInObject() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    final PsiFile[] files = myFixture.configureByFiles("stateReferences.navigation.html", "appStateWithNameInObject.js", "angular.js");
    myFixture.doHighlighting();
    checkNavigation(files[0], "one", null, "appStateWithNameInObject.js");
    checkNavigation(files[0], "two", null, "appStateWithNameInObject.js");
    checkNavigation(files[0], "two.words", null, "appStateWithNameInObject.js");
    checkNavigation(files[0], ".words", "two.words", "appStateWithNameInObject.js");
  }

  private void checkNavigation(PsiFile file, String state, String referencedTextOrNull, String appStatesFileName) {
    ReadAction.run(() -> {
      String referencedTextExpected = referencedTextOrNull == null ? state : referencedTextOrNull;
      final PsiElement inObj = getElement(file, "ui-sref=\"" + state + "\"");
      assertEquals("ui-sref", inObj.getText());
      assertTrue(inObj.getParent() instanceof XmlAttribute);
      final XmlAttributeValue element = ((XmlAttribute)inObj.getParent()).getValueElement();
      assertNotNull(element);

      final PsiReference reference = element.getReference();
      assertEquals(state, reference.getCanonicalText());

      final ResolveResult[] results = ((PsiPolyVariantReference)reference).multiResolve(false);
      if (results.length > 1) {
        for (ResolveResult result : results) {
          final PsiElement resolvedElement = result.getElement();
          assertEquals(appStatesFileName, resolvedElement.getContainingFile().getName());
          if (StringUtil.equals(referencedTextExpected, StringUtil.unquoteString(resolvedElement.getNavigationElement().getText()))) {
            return;
          }
        }
        fail("Not found " + referencedTextExpected + " among results");
      } else {
        final PsiElement resolve = reference.resolve();
        assertNotNull(resolve, state);
        assertEquals(appStatesFileName, resolve.getContainingFile().getName());
        assertEquals(referencedTextExpected, StringUtil.unquoteString(resolve.getNavigationElement().getText()));
      }
    });
  }
}
