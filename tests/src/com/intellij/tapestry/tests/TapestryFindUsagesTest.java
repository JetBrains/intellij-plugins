package com.intellij.tapestry.tests;

import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.lang.findUsages.LanguageFindUsages;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Query;

import java.io.IOException;

/**
 * @author Alexey Chmutov
 *         Date: Dec 11, 2009
 *         Time: 4:38:39 PM
 */
public class TapestryFindUsagesTest extends TapestryBaseTestCase {
  @Override
  protected String getBasePath() {
    return "findUsages/";
  }

  public void testAccessorAsPropertyUsage() throws Throwable {
    addComponentToProject("Count");
    doTest("References to a Property", 3);
  }

  public void testPropertyAsMethodUsage() throws Throwable {
    addComponentToProject("Count");
    doTest("References to a method", 3);
  }

  private void doTest(final String message, final int refsExpected) throws IOException {
    initByComponent();
    PsiReference[] refs = findUsagesOfElementAtCaret();
    assertEquals(message, refsExpected, refs.length);
  }

  private PsiReference[] findUsagesOfElementAtCaret() throws IOException {

    FindUsagesProvider handler = LanguageFindUsages.INSTANCE.forLanguage(myFixture.getFile().getLanguage());
    PsiElement referenceTo = resolveReferenceAtCaretPosition();
    assertTrue("Wrong FindUsagesProvider", handler.canFindUsagesFor(referenceTo));
    final GlobalSearchScope scope = GlobalSearchScope.projectScope(myFixture.getProject());
    Query<PsiReference> query = referenceTo instanceof PsiMethod
                                ? MethodReferencesSearch.search((PsiMethod)referenceTo, scope, false)
                                : ReferencesSearch.search(referenceTo, scope, true);
    return query.toArray(PsiReference.EMPTY_ARRAY);
  }

}
