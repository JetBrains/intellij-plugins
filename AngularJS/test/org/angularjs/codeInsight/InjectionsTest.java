package org.angularjs.codeInsight;

import com.intellij.lang.javascript.psi.JSDefinitionExpression;
import com.intellij.lang.javascript.psi.JSNamedElement;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.resolve.ImplicitJSVariableImpl;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class InjectionsTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "injections";
  }

  @Override
  protected boolean isWriteActionRequired() {
    return getTestName(true).contains("Completion");
  }

  public void testNgInitCompletion() {
    myFixture.testCompletion("ngInit.html", "ngInit.after.html", "angular.js");
  }

  public void testNgInitResolve() {
    myFixture.configureByFiles("ngInit.resolve.html", "angular.js");
    checkVariableResolve("fri<caret>ends", "friends", JSVariable.class);
  }

  public void testNgRepeatImplicitCompletion() {
    myFixture.configureByFiles("ngRepeatImplicit.html", "angular.js");
    myFixture.testCompletionVariants("ngRepeatImplicit.html", "$index", "$first", "$middle", "$last", "$even", "$odd");
  }

  public void testNgRepeatAfterDotCompletion() {
    myFixture.configureByFiles("ngRepeatImplicitAfterDot.html", "angular.js");
    final List<String> variants = myFixture.getCompletionVariants("ngRepeatImplicitAfterDot.html");
    assertDoesntContain(variants, "person", "$index", "$first", "$middle", "$last", "$even", "$odd");
  }

  public void testNgRepeatImplicitResolve() {
    myFixture.configureByFiles("ngRepeatImplicitType.html", "angular.js");
    final PsiElement resolve = checkVariableResolve("ind<caret>ex", "$index", ImplicitJSVariableImpl.class);
    assertEquals("Number", ((JSVariable)resolve).getTypeString());
  }

  public void testNgRepeatExplicitCompletion() {
    myFixture.testCompletion("ngRepeatExplicit.html", "ngRepeatExplicit.after.html", "angular.js");
  }

  public void testNgRepeatExplicitResolve() {
    myFixture.configureByFiles("ngRepeatExplicit.resolve.html", "angular.js");
    checkVariableResolve("per<caret>son", "person", JSDefinitionExpression.class);
  }

  public void testNgRepeatExplicitKeyCompletion() {
    myFixture.testCompletion("ngRepeatExplicitHashKey.html", "ngRepeatExplicitHashKey.after.html", "angular.js");
  }

  public void testNgRepeatExplicitKeyResolve() {
    myFixture.configureByFiles("ngRepeatExplicitHashKey.resolve.html", "angular.js");
    checkVariableResolve("ke<caret>y", "key", JSDefinitionExpression.class);
  }

  public void testNgRepeatExplicitValueCompletion() {
    myFixture.testCompletion("ngRepeatExplicitHashValue.html", "ngRepeatExplicitHashValue.after.html", "angular.js");
  }

  public void testNgRepeatExplicitValueResolve() {
    myFixture.configureByFiles("ngRepeatExplicitHashValue.resolve.html", "angular.js");
    checkVariableResolve("val<caret>ue", "value", JSDefinitionExpression.class);
  }

  public void testNgControllerAliasCompletion() {
    myFixture.testCompletion("ngControllerAlias.html", "ngControllerAlias.after.html", "angular.js", "custom.js");
  }

  public void testNgControllerAliasHighlighting() {
    myFixture.configureByFiles("ngControllerAlias.highlight.html", "angular.js", "custom.js");
    myFixture.checkHighlighting();
  }

  public void testNgControllerAliasResolve() {
    myFixture.configureByFiles("ngControllerAlias.resolve.html", "angular.js", "custom.js");
    checkVariableResolve("ap<caret>p", "app", JSDefinitionExpression.class);
  }

  private PsiElement checkVariableResolve(final String signature, final String varName, final Class<? extends JSNamedElement> varClass) {
    int offsetBySignature = AngularTestUtil.findOffsetBySignature(signature, myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertInstanceOf(resolve, varClass);
    assertEquals(varName, varClass.cast(resolve).getName());
    return resolve;
  }
}
