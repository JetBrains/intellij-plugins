// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.steps.search;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.plugins.cucumber.CucumberUtil;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;

import java.util.List;

@NotNullByDefault
final class CucumberJavaSearchUtil {
  private CucumberJavaSearchUtil() {}

  static void findGherkinReferencesToMethod(Processor<? super PsiReference> consumer, SearchScope searchScope, PsiMethod method) {
    if (CucumberJavaUtil.isAnnotationStepDefinition(method)) {
      List<PsiAnnotation> stepAnnotations = CucumberJavaUtil.getCucumberStepAnnotations(method);
      for (PsiAnnotation stepAnnotation : stepAnnotations) {
        String regexp = CucumberJavaUtil.getPatternFromStepDefinition(stepAnnotation);
        if (regexp != null) {
          CucumberUtil.findGherkinReferencesToElement(stepAnnotation, regexp, consumer, searchScope);
        }
      }
    }
  }
}
