package com.intellij.tapestry.intellij;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;

import java.util.Arrays;
import java.util.Collection;

public class TapestryImplicitUsageProvider implements ImplicitUsageProvider {
  private static final Collection<String> ourAnnotations = Arrays.asList(
    "org.apache.tapestry5.ioc.annotations.Inject", "org.apache.tapestry5.annotations.Component");

  @Override
  public boolean isImplicitUsage(PsiElement element) {
    return false;
  }

  @Override
  public boolean isImplicitRead(PsiElement element) {
    return false;
  }

  @Override
  public boolean isImplicitWrite(PsiElement element) {
    if (element instanceof PsiField && AnnotationUtil.isAnnotated((PsiField)element, ourAnnotations, false)) {
      return true;
    }
    return false;
  }
}
