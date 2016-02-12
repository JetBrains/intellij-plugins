package org.angularjs.codeInsight;

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase;
import com.intellij.lang.javascript.psi.impl.JSOffsetBasedImplicitElement;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;
import org.junit.Assert;

import java.util.List;

/**
 * @author Irina.Chernushina on 2/12/2016.
 */
public class AngularUiRouterTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "uiRouter";
  }

  @Override
  protected boolean isWriteActionRequired() {
    return false;
  }

  public void testCompletion() throws Exception {
    final List<String> variants = myFixture.getCompletionVariants("app1.js", "one.html", "two.html", "angular.js");
    Assert.assertEquals("menuTip", variants.get(0));
  }

  public void testNavigationToView1() throws Exception {
    final PsiFile[] files = myFixture.configureByFiles("app.js", "one.html", "two.html", "angular.js");
    myFixture.doHighlighting();

    final String str = "'menuTip'";
    final int idx = myFixture.getEditor().getDocument().getText().indexOf(str);
    Assert.assertTrue(idx > 0);
    final PsiElement element = files[0].findElementAt(idx);
    Assert.assertNotNull(element);
    Assert.assertTrue(element.getParent() instanceof JSProperty);
    final PsiReference reference = element.getParent().getReference();
    Assert.assertNotNull(reference);
    Assert.assertEquals(StringUtil.unquoteString(str), reference.getCanonicalText());

    final PsiElement resolve = reference.resolve();
    Assert.assertNotNull(resolve);
    Assert.assertEquals("one.html", resolve.getContainingFile().getName());
    Assert.assertEquals(StringUtil.unquoteString(str), ((JSPsiNamedElementBase) resolve).getName());
  }

  public void testNavigationToView2() throws Exception {
    final PsiFile[] files = myFixture.configureByFiles("app.js", "one.html", "two.html", "angular.js");
    myFixture.doHighlighting();

    emptyViewNavigatesToFilesDefaultView(files[0], "'one.html'");
    emptyViewNavigatesToFilesDefaultView(files[0], "'two.html'");
  }

  private void emptyViewNavigatesToFilesDefaultView(PsiFile file, String str) {
    final int idx = myFixture.getEditor().getDocument().getText().indexOf(str);
    Assert.assertTrue(idx > 0);
    final PsiElement inObj = file.findElementAt(idx);
    Assert.assertNotNull(inObj);
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
  public void testStatesCompletion() throws Exception {
    final List<String> variants = myFixture.getCompletionVariants("stateReferences1.html", "appStates.js", "angular.js");
    Assert.assertTrue(variants.contains("one"));
    Assert.assertTrue(variants.contains("two"));
    Assert.assertTrue(variants.contains("two.words"));
  }

  public void testStatesNavigation() throws Exception {
    final PsiFile[] files = myFixture.configureByFiles("stateReferences.html", "appStates.js", "angular.js");
    myFixture.doHighlighting();
    checkNavigation(files[0], "one");
    checkNavigation(files[0], "two");
    checkNavigation(files[0], "two.words");
  }

  private void checkNavigation(PsiFile file, String state) {
    final int idx = myFixture.getEditor().getDocument().getText().indexOf("ui-sref=\"" + state + "\"");
    Assert.assertTrue(idx > 0);
    final PsiElement inObj = file.findElementAt(idx);
    Assert.assertNotNull(inObj);
    Assert.assertEquals("ui-sref", inObj.getText());
    Assert.assertTrue(inObj.getParent() instanceof XmlAttribute);
    final XmlAttributeValue element = ((XmlAttribute)inObj.getParent()).getValueElement();
    Assert.assertNotNull(element);

    final PsiReference reference = element.getReference();
    Assert.assertEquals(state, reference.getCanonicalText());

    final PsiElement resolve = reference.resolve();
    Assert.assertNotNull(resolve);
    Assert.assertEquals("appStates.js", resolve.getContainingFile().getName());
    Assert.assertEquals(state, ((JSPsiNamedElementBase) resolve).getName());
  }
}
