package org.angularjs.codeInsight;

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase;
import com.intellij.lang.javascript.psi.impl.JSOffsetBasedImplicitElement;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

import java.util.List;

public class AngularUiRouterTest extends BasePlatformTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "uiRouter";
  }


  public void testSimpleViewCompletion() {
    final List<String> variants = myFixture.getCompletionVariants("simpleView.completion.js", "one.html", "two.html", "angular.js");
    Assert.assertEquals("menuTip", variants.get(0));
  }

  public void testPartialTypedViewCompletion() {
    final List<String> variants = myFixture.getCompletionVariants("partialTypedView.completion.js", "one.html", "two.html", "angular.js");
    Assert.assertEquals("menuTip", variants.get(0));
  }

  public void testPartialTypedViewNavigation() {
    final PsiFile[] files = myFixture.configureByFiles("partialTypedView.navigation.js", "one.html", "two.html", "angular.js");
    myFixture.doHighlighting();

    testNavigationToMenuTip(files[0]);
  }

  public void testNoNavigationFromViews() {
    //when there is not a views definition
    final PsiFile[] files = myFixture.configureByFiles("noNavigationFromViews.js", "angular.js");
    myFixture.doHighlighting();

    final PsiElement another = getReferenceByTextAround(files[0], "another").resolve();
    Assert.assertTrue(another instanceof JSProperty);
    Assert.assertEquals("another", ((JSProperty) another).getName());
    final PsiElement boring = getReferenceByTextAround(files[0], "boring").resolve();
    Assert.assertTrue(boring instanceof JSProperty);
    Assert.assertEquals("boring", ((JSProperty) boring).getName());
  }

  private void testNavigationToMenuTip(PsiFile file) {
    final String str = "'menuTip'";
    final PsiReference reference = getReferenceByTextAround(file, str);
    Assert.assertEquals(StringUtil.unquoteString(str), reference.getCanonicalText());

    final PsiElement resolve = reference.resolve();
    Assert.assertNotNull(resolve);
    Assert.assertEquals("one.html", resolve.getContainingFile().getName());
    Assert.assertEquals(StringUtil.unquoteString(str), ((JSPsiNamedElementBase) resolve).getName());
  }

  public void testInnerPropertyControllerAs() {
    final List<String> variants = myFixture.getCompletionVariants("innerPropertyControllerAs.completion.js", "one.html", "two.html", "angular.js");
    Assert.assertEquals("testMe", variants.get(0));
  }

  public void testControllerRedefinitionSyntaxOutside() {
    final List<String> variants = myFixture.getCompletionVariants("controllerRedefinitionSyntaxOutside.completion.js", "one.html", "two.html", "angular.js");
    Assert.assertTrue(variants.contains("testMe"));
    Assert.assertTrue(variants.contains("something"));
  }

  public void testControllerRedefinitionSyntaxNavigation() {
    final String mainFile = "controllerRedefinitionSyntax.navigation.js";
    final PsiFile[] files = myFixture.configureByFiles(mainFile, "one.html", "two.html", "angular.js");
    myFixture.doHighlighting();
    final String str = "'testMe as something'";
    final PsiElement element = getElement(files[0], str);
    Assert.assertEquals(str, element.getText());
    Assert.assertTrue(element.getParent() instanceof JSLiteralExpression);
    Assert.assertTrue(element.getParent().getParent() instanceof JSProperty);

    final PsiReference[] references = element.getParent().getReferences();
    Assert.assertEquals(1, references.length);
    Assert.assertEquals("testMe", references[0].getCanonicalText());

    final PsiElement resolve = references[0].resolve();
    Assert.assertNotNull(resolve);
    Assert.assertEquals(mainFile, resolve.getContainingFile().getName());
    Assert.assertEquals("testMe", ((JSPsiNamedElementBase) resolve).getName());
  }

  public void testNavigationToNamedView() {
    final PsiFile[] files = myFixture.configureByFiles("appWithViews.navigation.js", "one.html", "two.html", "angular.js");
    myFixture.doHighlighting();

    testNavigationToMenuTip(files[0]);
  }

  @NotNull
  private PsiReference getReferenceByTextAround(PsiFile file, String str) {
    final PsiElement element = getElement(file, str);
    Assert.assertTrue(element.getParent() instanceof JSProperty);
    PsiReference reference = element.getParent().getReference();
    Assert.assertNotNull(reference);
    return reference;
  }

  @NotNull
  private PsiElement getElement(PsiFile file, String str) {
    final int idx = myFixture.getEditor().getDocument().getText().indexOf(str);
    Assert.assertTrue(idx > 0);
    final PsiElement element = file.findElementAt(idx);
    Assert.assertNotNull(element);
    return element;
  }

  public void testNavigationToDefaultView() {
    final PsiFile[] files = myFixture.configureByFiles("appWithViews.navigation.js", "one.html", "two.html", "angular.js");
    myFixture.doHighlighting();

    emptyViewNavigatesToFilesDefaultView(files[0], "'one.html'");
    emptyViewNavigatesToFilesDefaultView(files[0], "'two.html'");
  }

  private void emptyViewNavigatesToFilesDefaultView(PsiFile file, String str) {
    final PsiElement inObj = getElement(file, str);
    Assert.assertTrue(inObj.getParent() instanceof JSLiteralExpression);
    Assert.assertTrue(inObj.getParent().getParent() instanceof JSProperty);
    final JSProperty templateUrl = (JSProperty)inObj.getParent().getParent();
    Assert.assertEquals("templateUrl", templateUrl.getName());
    final PsiElement parentProperty = templateUrl.getParent().getParent();
    Assert.assertTrue(parentProperty instanceof JSProperty);
    final PsiReference reference = parentProperty.getReference();
    Assert.assertNotNull(reference);
    Assert.assertEquals("", reference.getCanonicalText());

    final PsiElement resolve = reference.resolve();
    Assert.assertNotNull(resolve);
    Assert.assertEquals(StringUtil.unquoteString(str), resolve.getContainingFile().getName());
    Assert.assertEquals("", ((JSPsiNamedElementBase) resolve).getName());

    final PsiElement element = ((JSOffsetBasedImplicitElement)resolve).getElementAtOffset();
    Assert.assertEquals("ui-view", element.getText());
  }

  // states
  public void testStatesCompletion() {
    final List<String> variants = myFixture.getCompletionVariants("stateReferences.completion.html", "appStates.js", "angular.js");
    Assert.assertTrue(variants.contains("one"));
    Assert.assertTrue(variants.contains("two"));
    Assert.assertTrue(variants.contains("two.words"));
  }

  public void testStatesNavigation() {
    final PsiFile[] files = myFixture.configureByFiles("stateReferences.navigation.html", "appStates.js", "angular.js");
    myFixture.doHighlighting();
    checkNavigation(files[0], "one", null, "appStates.js");
    checkNavigation(files[0], "two", null, "appStates.js");
    checkNavigation(files[0], "two.words", null, "appStates.js");
    checkNavigation(files[0], ".words", "two.words", "appStates.js");
  }

  public void testStatesNavigationForStatesWithNameInObject() {
    final PsiFile[] files = myFixture.configureByFiles("stateReferences.navigation.html", "appStateWithNameInObject.js", "angular.js");
    myFixture.doHighlighting();
    checkNavigation(files[0], "one", null, "appStateWithNameInObject.js");
    checkNavigation(files[0], "two", null, "appStateWithNameInObject.js");
    checkNavigation(files[0], "two.words", null, "appStateWithNameInObject.js");
    checkNavigation(files[0], ".words", "two.words", "appStateWithNameInObject.js");
  }

  private void checkNavigation(PsiFile file, String state, String referencedTextExpected, String appStatesFileName) {
    referencedTextExpected = referencedTextExpected == null ? state : referencedTextExpected;
    final PsiElement inObj = getElement(file, "ui-sref=\"" + state + "\"");
    Assert.assertEquals("ui-sref", inObj.getText());
    Assert.assertTrue(inObj.getParent() instanceof XmlAttribute);
    final XmlAttributeValue element = ((XmlAttribute)inObj.getParent()).getValueElement();
    Assert.assertNotNull(element);

    final PsiReference reference = element.getReference();
    Assert.assertEquals(state, reference.getCanonicalText());

    final ResolveResult[] results = ((PsiPolyVariantReference)reference).multiResolve(false);
    if (results.length > 1) {
      for (ResolveResult result : results) {
        final PsiElement resolvedElement = result.getElement();
        Assert.assertEquals(appStatesFileName, resolvedElement.getContainingFile().getName());
        if (StringUtil.equals(referencedTextExpected, StringUtil.unquoteString(resolvedElement.getNavigationElement().getText()))) {
          return;
        }
      }
      Assert.fail("Not found " + referencedTextExpected + " among results");
    } else {
      final PsiElement resolve = reference.resolve();
      Assert.assertNotNull(state, resolve);
      Assert.assertEquals(appStatesFileName, resolve.getContainingFile().getName());
      Assert.assertEquals(referencedTextExpected, StringUtil.unquoteString(resolve.getNavigationElement().getText()));
    }
  }
}
