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

import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.util.containers.ContainerUtil;
import com.thoughtworks.gauge.StepValue;
import com.thoughtworks.gauge.helper.ModuleHelper;
import com.thoughtworks.gauge.language.psi.SpecPsiImplUtil;
import com.thoughtworks.gauge.language.psi.SpecStep;
import com.thoughtworks.gauge.util.StepUtil;

import java.util.List;
import java.util.stream.Stream;

final class AnnotationHelper {
  private static final ModuleHelper helper = new ModuleHelper();

  boolean isImplemented(SpecStep step, Module module) {
    return StepUtil.isImplementedStep(step, module);
  }

  boolean isEmpty(SpecStep step) {
    return step.getStepValue().getStepText().trim().isEmpty();
  }

  boolean isGaugeModule(PsiElement element) {
    return helper.isGaugeModule(element);
  }

  Module getModule(SpecStep step) {
    return helper.getModule(step);
  }

  Stream<StepValue> getStepValues(PsiMethod psiElement) {
    return StepUtil.getGaugeStepAnnotationValues(psiElement).stream()
      .map(s -> SpecPsiImplUtil.getStepValueFor(psiElement, s, false));
  }
}
