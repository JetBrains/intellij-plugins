// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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
  @Override
  public final @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    OsmorcFacet facet = OsmorcFacet.getInstance(holder.getFile());
    return facet == null ? PsiElementVisitor.EMPTY_VISITOR : buildVisitor(facet, holder, isOnTheFly);
  }

  protected abstract @NotNull PsiElementVisitor buildVisitor(OsmorcFacet facet, ProblemsHolder holder, boolean isOnTheFly);

  protected static @Nullable PsiElement unwrap(@Nullable PsiElement element) {
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