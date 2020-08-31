/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.thoughtworks.gauge.GaugeBundle;
import com.thoughtworks.gauge.StepValue;
import org.jetbrains.annotations.NotNull;

public final class ParamAnnotator implements Annotator {
  private final AnnotationHelper helper;

  public ParamAnnotator() {
    this.helper = new AnnotationHelper();
  }

  @Override
  public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder holder) {
    if (psiElement instanceof PsiMethod && helper.isGaugeModule(psiElement)) {
      helper.getStepValues((PsiMethod)psiElement)
        .filter(value -> value.getParameters().size() != ((PsiMethod)psiElement).getParameterList().getParametersCount())
        .forEach(value -> createWarning((PsiMethod)psiElement, holder, value.getStepAnnotationText(), value));
    }
  }

  private static void createWarning(@NotNull PsiMethod psiElement,
                                    @NotNull AnnotationHolder holder,
                                    @NlsSafe String alias,
                                    StepValue value) {
    int actual = psiElement.getParameterList().getParametersCount();
    int expected = value.getParameters().size();

    holder.newAnnotation(
      HighlightSeverity.ERROR,
      GaugeBundle.message("inspection.message.parameter.count.mismatch.found.expected.with.step.annotation", actual, expected, alias))
      .range(psiElement.getParameterList().getTextRange())
      .create();
  }
}
