package com.intellij.tapestry.intellij;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

public class TapestryImplicitUsageProvider implements ImplicitUsageProvider {
  private static final Collection<String> ourAnnotations = Arrays.asList(
    "org.apache.tapestry5.ioc.annotations.Inject", "org.apache.tapestry5.annotations.Component");

  @Override
  public boolean isImplicitUsage(@NotNull PsiElement element) {
    return false;
  }

  @Override
  public boolean isImplicitRead(@NotNull PsiElement element) {
    return false;
  }

  @Override
  public boolean isImplicitWrite(@NotNull PsiElement element) {
    return element instanceof PsiField && AnnotationUtil.isAnnotated((PsiField)element, ourAnnotations, 0);
  }
}