package com.jetbrains.lang.dart.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveVisitor;

public class DartRecursiveVisitor extends DartVisitor implements PsiRecursiveVisitor {
  @Override
  public void visitElement(PsiElement element) {
    element.acceptChildren(this);
  }
}
