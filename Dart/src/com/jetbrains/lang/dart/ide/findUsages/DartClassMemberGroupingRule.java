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
import com.intellij.usages.UsageTarget;
import com.intellij.usages.rules.PsiElementUsage;
import com.intellij.usages.rules.SingleParentUsageGroupingRule;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.psi.DartClassMembers;
import com.jetbrains.lang.dart.psi.DartComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartClassMemberGroupingRule extends SingleParentUsageGroupingRule implements DumbAware {
  @Nullable
  @Override
  protected UsageGroup getParentGroupFor(@NotNull Usage usage, @NotNull UsageTarget[] targets) {
    PsiElement psiElement = usage instanceof PsiElementUsage ? ((PsiElementUsage)usage).getElement() : null;
    if (psiElement == null || psiElement.getLanguage() != DartLanguage.INSTANCE) return null;

    // todo Docs are not parsed perfectly and doc comment may be not a child of the corresponding function. Related to comment for DartDocUtil.getDocumentationText

    while (psiElement != null) {
      if (psiElement instanceof DartComponent && psiElement.getParent() instanceof DartClassMembers) {
        final DartComponent componentElement = (DartComponent)psiElement;
        return new DartComponentUsageGroup(componentElement);
      }
      psiElement = psiElement.getParent();
    }

    return null;
  }
}
