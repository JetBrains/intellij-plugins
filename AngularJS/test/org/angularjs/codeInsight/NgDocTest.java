package org.angularjs.codeInsight;

import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

/**
 * @author Konstantin.Ulitin
 */
public class NgDocTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "ngDoc";
  }

  public void testModule15() {
    myFixture.configureByFiles(getTestName(false) + ".js");
    PsiReference ref = myFixture.getFile().findReferenceAt(myFixture.getCaretOffset());
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertInstanceOf(resolve, JSImplicitElement.class);
  }
}
