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
import com.intellij.psi.PsiElement;
import com.thoughtworks.gauge.GaugeBundle;
import com.thoughtworks.gauge.language.psi.ConceptStep;
import com.thoughtworks.gauge.language.psi.SpecStep;
import com.thoughtworks.gauge.language.psi.impl.SpecStepImpl;
import org.jetbrains.annotations.NotNull;

final class StepAnnotator implements Annotator {
  private final AnnotationHelper helper;

  StepAnnotator() {
    this.helper = new AnnotationHelper();
  }

  @Override
  public void annotate(final @NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    if (!helper.isGaugeModule(element)) return;
    if (element instanceof SpecStep) {
      createWarning(element, holder, (SpecStep)element);
    }
    else if (element instanceof ConceptStep) {
      SpecStepImpl step = new SpecStepImpl(element.getNode());
      step.setConcept(true);
      createWarning(element, holder, step);
    }
  }

  private void createWarning(PsiElement element, AnnotationHolder holder, SpecStep step) {
    if (helper.isEmpty(step)) {
      holder.newAnnotation(HighlightSeverity.ERROR, GaugeBundle.message("inspection.message.step.should.be.blank"))
        .range(element.getTextRange())
        .create();
    }
    else if (!helper.isImplemented(step, helper.getModule(step))) {
      holder.newAnnotation(HighlightSeverity.ERROR, GaugeBundle.message("inspection.message.undefined.step"))
        .range(element.getTextRange())
        .withFix(new CreateStepImplFix(step))
        .create();
    }
  }
}
