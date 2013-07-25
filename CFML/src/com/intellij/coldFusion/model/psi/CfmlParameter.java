package com.intellij.coldFusion.model.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

public interface CfmlParameter extends PsiElement, PsiNamedElement {
  boolean isRequired();
  @Nullable
  String getType();
  @Nullable @NonNls
  String getName();
}
