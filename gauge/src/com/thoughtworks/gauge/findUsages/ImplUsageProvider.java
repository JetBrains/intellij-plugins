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

import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.intellij.psi.impl.source.PsiParameterImpl;
import com.thoughtworks.gauge.findUsages.helper.ReferenceSearchHelper;
import com.thoughtworks.gauge.helper.ModuleHelper;
import com.thoughtworks.gauge.util.GaugeUtil;
import com.thoughtworks.gauge.util.HookUtil;
import com.thoughtworks.gauge.util.StepUtil;
import org.jetbrains.annotations.NotNull;

public final class ImplUsageProvider implements ImplicitUsageProvider {
  private final ReferenceSearchHelper searchHelper;
  private final ModuleHelper moduleHelper;

  public ImplUsageProvider() {
    this.searchHelper = new ReferenceSearchHelper();
    this.moduleHelper = new ModuleHelper();
  }

  @Override
  public boolean isImplicitUsage(@NotNull PsiElement element) {
    if (!moduleHelper.isGaugeModule(element)) return false;
    if (element instanceof PsiClassImpl) return isClassUsed((PsiClassImpl)element);
    if (element instanceof PsiParameterImpl) return isParameterUsed((PsiParameterImpl)element);
    return isElementUsed(element);
  }

  private static boolean isClassUsed(PsiClassImpl element) {
    for (PsiMethod psiMethod : element.getMethods()) {
      if (StepUtil.getGaugeStepAnnotationValues(psiMethod).size() > 0 || HookUtil.isHook(psiMethod)) return true;
    }
    return false;
  }

  private static boolean isParameterUsed(PsiParameterImpl element) {
    return GaugeUtil.isGaugeElement(element.getDeclarationScope());
  }

  private boolean isElementUsed(PsiElement element) {
    if (HookUtil.isHook(element)) return true;
    boolean isGaugeElement = GaugeUtil.isGaugeElement(element);
    if (!isGaugeElement) return false;
    StepCollector collector = new StepCollector(element.getProject());
    collector.collect();
    return searchHelper.getPsiElements(collector, element).size() > 0;
  }

  @Override
  public boolean isImplicitRead(@NotNull PsiElement element) {
    return false;
  }

  @Override
  public boolean isImplicitWrite(@NotNull PsiElement element) {
    return false;
  }
}
