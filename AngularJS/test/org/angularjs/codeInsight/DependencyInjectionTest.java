package org.angularjs.codeInsight;

import com.intellij.lang.javascript.psi.JSDefinitionExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
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
    assertInstanceOf(JSResolveUtil.unwrapProxy(resolve), JSProperty.class);
  }

  public void testInjectedStaticResolve() {
    myFixture.configureByFiles("static.js", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("DomUtils.createSVG<caret>ElementTemplate(", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertInstanceOf(JSResolveUtil.unwrapProxy(resolve), JSDefinitionExpression.class);
  }
}
