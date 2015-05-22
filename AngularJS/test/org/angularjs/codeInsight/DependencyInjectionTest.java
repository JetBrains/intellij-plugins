package org.angularjs.codeInsight;

import com.intellij.lang.javascript.psi.JSDefinitionExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

/**
 * @author Dennis.Ushakov
 */
public class DependencyInjectionTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "di";
  }

  @Override
  protected boolean isWriteActionRequired() {
    return getTestName(true).contains("Completion");
  }

  public void testInjectedServiceCompletion() {
    myFixture.testCompletionTyping("di.js", "\n", "di.after.js", "angular.js");
  }

  public void testInjectedServiceResolve() {
    myFixture.configureByFiles("di.resolve.js", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("myService.fo<caret>o();", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertInstanceOf(resolve, JSProperty.class);
  }

  public void testInjectedConstantCompletion() {
    myFixture.testCompletionTyping("constant.js", "\n", "constant.after.js", "angular.js");
  }

  public void testInjectedConstantResolve() {
    myFixture.configureByFiles("constant.resolve.js", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("myService.fo<caret>o();", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertInstanceOf(resolve, JSProperty.class);
  }

  public void testInjectedStaticResolve() {
    myFixture.configureByFiles("static.js", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("DomUtils.createSVG<caret>ElementTemplate(", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertInstanceOf(resolve, JSDefinitionExpression.class);
  }

  public void testInjectedParameterCompletion() {
    myFixture.testCompletion("parameter.js", "parameter.after.js", "angular.js");
  }

  public void testInjectedParameterResolve() {
    myFixture.configureByFiles("parameter.resolve.js", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("function (my<caret>Service)", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertInstanceOf(resolve, JSImplicitElement.class);
    assertEquals("'myService'", AngularTestUtil.getDirectiveDefinitionText(resolve));
  }

  public void testInjectedParameterToVarResolve() {
    myFixture.configureByFiles("parameterToVar.resolve.js", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("function (my<caret>Service)", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertInstanceOf(resolve, JSVariable.class);
  }

  public void testInjectedNamedParameterCompletion() {
    myFixture.testCompletion("namedParameter.js", "namedParameter.after.js", "angular.js");
  }

  public void testInjectedNamedParameterResolve() {
    myFixture.configureByFiles("namedParameter.resolve.js", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("function (my<caret>Service)", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertInstanceOf(resolve, JSImplicitElement.class);
    assertEquals("'myService'", AngularTestUtil.getDirectiveDefinitionText(resolve));
  }
}
