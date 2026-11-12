// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.lang.findUsages.LanguageFindUsages;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.sharedCodeInsightProjectFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestApplication
public class JadeFindUsagesTest {
  private static final TestFixture<Project> projectFixture = sharedCodeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> fixture = sharedCodeInsightFixture(projectFixture);
  private String myTestName;

  @BeforeEach
  void setUp(TestInfo testInfo) {
    myTestName = PlatformTestUtil.getTestName(testInfo.getTestMethod().orElseThrow().getName(), true);
    fixture.get().setTestDataPath(JadeTestUtil.getBaseTestDataPath() + "/findUsages/");
  }

  @Test
  public void testCssSelector() {
    fixture.get().configureByFile(myTestName + ".jade");
    final PsiReference[] usages = findUsages();
    assertEquals(4, usages.length);
  }

  @Test
  public void testConstVar() {
    fixture.get().configureByFile(myTestName + ".jade");
    final PsiReference[] usages = findUsages();
    assertEquals(3, usages.length);
  }

  private PsiReference[] findUsages() {
    CodeInsightTestFixture myFixture = fixture.get();
    return ReadAction.computeBlocking(() -> {
      final PsiElement element =
        TargetElementUtil.findTargetElement(myFixture.getEditor(), TargetElementUtil
                                                                     .ELEMENT_NAME_ACCEPTED | TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED);
      assertNotNull(element);
      assertTrue(LanguageFindUsages.canFindUsagesFor(element), "Cannot find element in caret");
      return ReferencesSearch.search(element, GlobalSearchScope.allScope(myFixture.getProject()), false).toArray(PsiReference.EMPTY_ARRAY);
    });
  }
}
