/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
package org.osmorc.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.facet.OsmorcFacet;

public abstract class AbstractOsgiVisitor extends LocalInspectionTool {
  @NotNull
  @Override
  public final PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    OsmorcFacet facet = OsmorcFacet.getInstance(holder.getFile());
    return facet == null ? PsiElementVisitor.EMPTY_VISITOR : buildVisitor(facet, holder, isOnTheFly);
  }

  @NotNull
  protected abstract PsiElementVisitor buildVisitor(OsmorcFacet facet, ProblemsHolder holder, boolean isOnTheFly);

  @Nullable
  protected static PsiElement unwrap(@Nullable PsiElement element) {
    if (element != null && !element.isPhysical()) {
      PsiElement navigationElement = element.getNavigationElement();
      if (navigationElement != null) {
        return navigationElement;
      }
    }
    return element;
  }

  @Contract("null -> false")
  protected boolean isValidElement(@Nullable PsiElement element) {
    return element != null && element.isPhysical() && !StringUtil.isEmpty(element.getText());
  }
}