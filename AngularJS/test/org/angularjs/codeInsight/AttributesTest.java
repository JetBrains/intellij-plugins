package org.angularjs.codeInsight;

import com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspectionBase;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

/**
 * @author Dennis.Ushakov
 */
public class AttributesTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "attributes";
  }

  @Override
  protected boolean isWriteActionRequired() {
    return getTestName(true).contains("Completion");
  }

  public void testStandardAttributesCompletion() {
    myFixture.testCompletion("simple.html", "simple.after.html");
  }

  public void testCustomAttributesCompletion() {
    myFixture.testCompletion("custom.html", "custom.after.html", "custom.js");
  }

  public void testCustomAttributesResolve() {
    myFixture.configureByFiles("custom.after.html", "custom.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("my-cus<caret>tomer", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("custom.js", resolve.getContainingFile().getName());
  }

  public void testNormalization() {
    myFixture.configureByFiles("normalize.html");
    myFixture.enableInspections(HtmlUnknownAttributeInspectionBase.class);
    myFixture.checkHighlighting();
  }
}
