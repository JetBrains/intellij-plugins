package org.jetbrains.plugins.cucumber.inspections.model;

import com.intellij.codeInspection.InspectionSuppressor;
import com.intellij.codeInspection.SuppressQuickFix;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.inspections.suppress.GherkinSuppressionUtil;

public class GherkinInspectionSuppressor implements InspectionSuppressor {
  @Override
  public boolean isSuppressedFor(@NotNull PsiElement element, @NotNull String toolId) {
    return GherkinSuppressionUtil.isSuppressedFor(element, toolId);
  }

  @NotNull
  @Override
  public SuppressQuickFix[] getSuppressActions(@Nullable PsiElement element, @NotNull String toolId) {
    return GherkinSuppressionUtil.getDefaultSuppressActions(toolId);
  }
}
