package org.intellij.plugins.postcss.resolve;

import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.testFramework.TestDataPath;
import org.intellij.plugins.postcss.PostCssFixtureTestCase;
import org.intellij.plugins.postcss.psi.PostCssCustomSelector;
import org.jetbrains.annotations.NotNull;

@TestDataPath("$CONTENT_ROOT/testData/resolve/customSelectors")
public class PostCssCustomSelectorResolveTest extends PostCssFixtureTestCase {

  public void testResolve() throws Throwable {
    doTest();
  }

  public void testResolveInCustomSelectorAtRule() throws Throwable {
    doTest();
  }

  public void testResolveWithImport() throws Throwable {
    final PsiReference reference = myFixture.getReferenceAtCaretPosition("invocation.pcss", "definition.pcss");
    assertNotNull(reference);
    final ResolveResult[] results = ((PsiPolyVariantReference)reference).multiResolve(false);
    assertTrue(results.length == 1);
    assertTrue(results[0].isValidResult());

    PostCssCustomSelector selector = (PostCssCustomSelector)results[0].getElement();
    assertEquals("button", selector.getName());
    assertEquals("definition.pcss", selector.getContainingFile().getName());
  }

  public void testResolveWithoutImport() throws Throwable {
    final PsiReference reference = myFixture.getReferenceAtCaretPosition("invocationWithoutImport.pcss", "definition.pcss");
    assertNotNull(reference);
    final ResolveResult[] results = ((PsiPolyVariantReference)reference).multiResolve(false);
    assertTrue(results.length == 0);
  }

  public void testResolveMulti() throws Throwable {
    final PsiReference reference = myFixture.getReferenceAtCaretPosition(getTestName(true) + ".pcss");
    assertNotNull(reference);
    final ResolveResult[] results = ((PsiPolyVariantReference)reference).multiResolve(false);
    assertTrue(results.length == 2);
    assertTrue(results[0].isValidResult());
    assertTrue(results[1].isValidResult());
  }

  private void doTest() {
    final PsiReference reference = myFixture.getReferenceAtCaretPosition(getTestName(true) + ".pcss");
    assertNotNull(reference);
    final ResolveResult[] results = ((PsiPolyVariantReference)reference).multiResolve(false);
    assertTrue(results.length == 1);
    assertTrue(results[0].isValidResult());
  }

  @Override
  @NotNull
  protected String getTestDataSubdir() {
    return "customSelectors";
  }
}