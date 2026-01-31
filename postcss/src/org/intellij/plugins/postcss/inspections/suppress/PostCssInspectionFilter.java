package org.intellij.plugins.postcss.inspections.suppress;

import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssTerm;
import com.intellij.psi.css.CssTermList;
import com.intellij.psi.css.inspections.CssApiBaseInspection;
import com.intellij.psi.css.inspections.CssInspectionFilter;
import com.intellij.psi.css.inspections.invalid.CssInvalidAtRuleInspection;
import com.intellij.psi.css.inspections.invalid.CssInvalidImportInspection;
import com.intellij.psi.css.inspections.invalid.CssInvalidNestedSelectorInspection;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import org.intellij.plugins.postcss.lexer.PostCssTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class PostCssInspectionFilter extends CssInspectionFilter {
  private static final Set<Class<? extends CssApiBaseInspection>> UNSUPPORTED_INSPECTIONS = ContainerUtil.newHashSet(
    CssInvalidImportInspection.class,
    CssInvalidAtRuleInspection.class,
    CssInvalidNestedSelectorInspection.class
  );

  @Override
  public boolean isSupported(@NotNull Class<? extends CssApiBaseInspection> clazz, @NotNull PsiElement context) {
    return !UNSUPPORTED_INSPECTIONS.contains(clazz);
  }

  @Override
  public boolean isValueShouldBeValidatedWithCssScheme(@Nullable CssTermList value) {
    if (value != null) {
      for (CssTerm term : PsiTreeUtil.findChildrenOfType(value, CssTerm.class)) {
        if (PsiTreeUtil.lastChild(term).getNode().getElementType() == PostCssTokenTypes.POST_CSS_SIMPLE_VARIABLE_TOKEN) {
          return false;
        }
      }
    }
    return super.isValueShouldBeValidatedWithCssScheme(value);
  }
}