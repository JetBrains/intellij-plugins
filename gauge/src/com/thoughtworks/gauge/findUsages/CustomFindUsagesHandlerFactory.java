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

package com.thoughtworks.gauge.findUsages;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesHandlerFactory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.thoughtworks.gauge.language.psi.impl.ConceptStepImpl;
import com.thoughtworks.gauge.language.psi.impl.SpecStepImpl;
import com.thoughtworks.gauge.util.KtUtil;
import com.thoughtworks.gauge.util.StepUtil;
import org.jetbrains.annotations.NotNull;

public final class CustomFindUsagesHandlerFactory extends FindUsagesHandlerFactory {
  @Override
  public boolean canFindUsages(@NotNull PsiElement psiElement) {
    if (psiElement instanceof PsiMethod) {
      return StepUtil.getGaugeStepAnnotationValues((PsiMethod)psiElement).size() > 0;
    } else if (KtUtil.isKtFunction(psiElement)) {
        return KtUtil.getGaugeStepAnnotationValues(psiElement).size() > 0;
    }
    return psiElement instanceof SpecStepImpl || psiElement instanceof ConceptStepImpl;
  }

  @Override
  public FindUsagesHandler createFindUsagesHandler(@NotNull PsiElement psiElement, boolean b) {
    return new StepFindUsagesHandler(psiElement);
  }
}
