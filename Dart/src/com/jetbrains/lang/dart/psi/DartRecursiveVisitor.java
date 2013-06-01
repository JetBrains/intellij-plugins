package com.jetbrains.lang.dart.psi;

import com.intellij.psi.PsiElement;

public class DartRecursiveVisitor extends DartVisitor {
  @Override
  public void visitElement(PsiElement element) {
    element.acceptChildren(this);
  }
}
