package com.jetbrains.lang.dart.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveVisitor;
import org.jetbrains.annotations.NotNull;

public class DartRecursiveVisitor extends DartVisitor implements PsiRecursiveVisitor {
  @Override
  public void visitElement(@NotNull PsiElement element) {
    element.acceptChildren(this);
  }
}
