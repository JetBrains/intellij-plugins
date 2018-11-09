package org.angularjs.codeInsight;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class FiltersTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass()) + "filters";
  }

  public void testFilterCompletion() {
    myFixture.configureByFiles("filter.html", "angular.js", "custom.js");
    final List<String> variants = myFixture.getCompletionVariants("filter.html");
    assertContainsElements(variants, "lowercase", "json", "filta");
  }

  public void testFilterBuiltinResolve() {
    myFixture.configureByFiles("filterBuiltin.resolve.html", "angular.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("js<caret>on", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("angular.js", resolve.getContainingFile().getName());
  }

  public void testFilterCustomResolve() {
    myFixture.configureByFiles("filterCustom.resolve.html", "angular.js", "custom.js");
    int offsetBySignature = AngularTestUtil.findOffsetBySignature("fil<caret>ta", myFixture.getFile());
    PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
    assertNotNull(ref);
    PsiElement resolve = ref.resolve();
    assertNotNull(resolve);
    assertEquals("custom.js", resolve.getContainingFile().getName());
    assertEquals("\"filta\"", resolve.getParent().getText());
  }

}
