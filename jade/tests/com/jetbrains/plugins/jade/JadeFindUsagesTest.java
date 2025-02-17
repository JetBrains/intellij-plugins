package com.jetbrains.plugins.jade;

import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.lang.findUsages.LanguageFindUsages;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.testFramework.LightPlatformCodeInsightTestCase;
import org.jetbrains.annotations.NotNull;

public class JadeFindUsagesTest extends LightPlatformCodeInsightTestCase {

  @NotNull
  @Override
  protected String getTestDataPath() {
    return JadeHighlightingTest.TEST_DATA_PATH + "/findUsages/";
  }

  public void testCssSelector() {
    configureByFile(getTestName(true) + ".jade");
    final PsiReference[] usages = findUsages();
    assertEquals(4, usages.length);
  }


  private PsiReference[] findUsages() {
    final PsiElement element =
      TargetElementUtil.findTargetElement(getEditor(), TargetElementUtil
                                                      .ELEMENT_NAME_ACCEPTED | TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED);
    assertNotNull(element);
    assertTrue("Cannot find element in caret", LanguageFindUsages.canFindUsagesFor(element));
    return ReferencesSearch.search(element, GlobalSearchScope.allScope(getProject()), false).toArray(PsiReference.EMPTY_ARRAY);
  }
}
