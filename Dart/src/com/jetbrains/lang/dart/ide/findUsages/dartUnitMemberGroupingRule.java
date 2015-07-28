/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.lang.dart.ide.findUsages;

import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.PsiElement;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageGroup;
import com.intellij.usages.rules.PsiElementUsage;
import com.intellij.usages.rules.UsageGroupingRule;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartFile;
import org.jetbrains.annotations.NotNull;

public class DartUnitMemberGroupingRule implements UsageGroupingRule, DumbAware {
  @Override
  public UsageGroup groupUsage(@NotNull Usage usage) {
    if (!(usage instanceof PsiElementUsage)) {
      return null;
    }
    PsiElement psiElement = ((PsiElementUsage)usage).getElement();

    while (psiElement != null) {
      if (psiElement instanceof DartComponent && psiElement.getParent() instanceof DartFile) {
        final DartComponent componentElement = (DartComponent)psiElement;
        return new DartComponentUsageGroup(componentElement);
      }
      psiElement = psiElement.getParent();
    }

    return null;
  }
}
