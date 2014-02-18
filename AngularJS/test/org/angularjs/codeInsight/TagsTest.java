package org.angularjs.codeInsight;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

/**
 * @author Dennis.Ushakov
 */
public class TagsTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "tags";
  }

  @Override
  protected boolean isWriteActionRequired() {
    return getTestName(true).contains("Completion");
  }

  public void testStandardAttributesCompletion() {
    myFixture.testCompletion("standard.html", "standard.after.html", "angular.js");
  }

  public void testStandardAttributesResolve() {
    myFixture.configureByFiles("standard.after.html", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("ng-fo<caret>rm", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("angular.js", resolve.getContainingFile().getName());
  }
}
