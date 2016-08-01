package org.intellij.plugins.postcss.inspections.suppress;

import com.intellij.psi.PsiElement;
import com.intellij.psi.css.inspections.CssApiBaseInspection;
import com.intellij.psi.css.inspections.CssInspectionFilter;
import com.intellij.psi.css.inspections.invalid.CssInvalidImportInspection;
import org.jetbrains.annotations.NotNull;

public class PostCssInspectionFilter extends CssInspectionFilter {
  @Override
  public boolean isSupported(@NotNull Class<? extends CssApiBaseInspection> clazz, @NotNull PsiElement context) {
    return !clazz.equals(CssInvalidImportInspection.class);
  }
}