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
    myFixture.configureByFile("definition.pcss");
    doTest();
  }

  public void testResolveWithoutImport() throws Throwable {
    myFixture.configureByFile("definition.pcss");
    doTest();
  }

  public void testResolveMulti() throws Throwable {
    doTest(2);
  }

  public void testResolveMultiInDifferentFiles() throws Throwable {
    myFixture.configureByFile("definition.pcss");
    doTest(2);
  }

  public void testInline() throws Throwable {
    doTest(1, "html");
  }

  private void doTest() {
    doTest(1, "pcss");
  }

  private void doTest(int count) {
    doTest(count, "pcss");
  }

  private void doTest(int count, String extension) {
    final PsiReference reference = myFixture.getReferenceAtCaretPosition(getTestName(true) + "." + extension);
    assertNotNull(reference);
    final ResolveResult[] results = ((PsiPolyVariantReference)reference).multiResolve(false);
    assertEquals(count, results.length);
    for (int i = 0; i < count; i++) {
      assertTrue(results[i].isValidResult());
    }
  }

  @Override
  @NotNull
  protected String getTestDataSubdir() {
    return "customSelectors";
  }
}