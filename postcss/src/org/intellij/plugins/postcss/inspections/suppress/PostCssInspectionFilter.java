package org.intellij.plugins.postcss.inspections.suppress;

import com.intellij.psi.PsiElement;
import com.intellij.psi.css.inspections.CssApiBaseInspection;
import com.intellij.psi.css.inspections.CssInspectionFilter;
import com.intellij.psi.css.inspections.invalid.CssInvalidAtRuleInspection;
import com.intellij.psi.css.inspections.invalid.CssInvalidImportInspection;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class PostCssInspectionFilter extends CssInspectionFilter {
  private static final Set<Class<? extends CssApiBaseInspection>> UNSUPPORTED_INSPECTIONS =
    ContainerUtil.newHashSet(CssInvalidImportInspection.class, CssInvalidAtRuleInspection.class);

  @Override
  public boolean isSupported(@NotNull Class<? extends CssApiBaseInspection> clazz, @NotNull PsiElement context) {
    return !UNSUPPORTED_INSPECTIONS.contains(clazz);
  }
}