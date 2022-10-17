package com.intellij.plugins.drools.lang.psi.searchers;

import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class DroolsFindUsagesProvider implements FindUsagesProvider {

  @Override
  public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
    return psiElement instanceof PsiNamedElement;
  }

  @Override
  public String getHelpId(@NotNull PsiElement psiElement) {
    return null;
  }

  @Override
  @NotNull @NonNls
  public String getType(@NotNull final PsiElement element) {
    if (element instanceof PsiClass) return "class";
    if (element instanceof PsiMethod) return "method";
    if (element instanceof PsiField) return "field";
    if (element instanceof PsiParameter) return "parameter";
    if (element instanceof PsiVariable ) return "variable";
    return "";
  }

  @Override
  @NotNull
  public String getDescriptiveName(@NotNull final PsiElement element) {
    if (element instanceof PsiNamedElement) {
      return StringUtil.notNullize(((PsiNamedElement)element).getName());
    }
    return "";
  }

  @Override
  @NotNull
  public String getNodeText(@NotNull final PsiElement element, final boolean useFullName) {
    final String name = element instanceof PsiNamedElement ? ((PsiNamedElement)element).getName() : null;
    return name != null ? name : element.getText();
  }
}
