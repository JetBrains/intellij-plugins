package org.intellij.plugins.postcss.resolve;

import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import org.intellij.plugins.postcss.PostCssFixtureTestCase;

public abstract class PostCssResolveTest extends PostCssFixtureTestCase {
  protected void doTest() {
    doTest(1, "pcss");
  }

  protected void doTest(int count) {
    doTest(count, "pcss");
  }

  protected void doTest(int count, String extension) {
    final PsiReference reference = myFixture.getReferenceAtCaretPosition(getTestName(true) + "." + extension);
    assertNotNull(reference);
    final ResolveResult[] results = ((PsiPolyVariantReference)reference).multiResolve(false);
    assertEquals(count, results.length);
    for (int i = 0; i < count; i++) {
      assertTrue(results[i].isValidResult());
    }
  }
}