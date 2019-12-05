// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveVisitor;
import org.jetbrains.annotations.NotNull;

public class CfmlRecursiveElementVisitor extends PsiElementVisitor implements PsiRecursiveVisitor {

  public static class Stop extends RuntimeException {
    public static final Stop DONE = new Stop();

    @Override
    public Throwable fillInStackTrace() {
      return this;
    }
  }

  @Override
  public void visitElement(@NotNull final PsiElement element) {
    element.acceptChildren(this);
  }

  public void visitCfmlFunction(CfmlFunction function) {
    visitElement(function);
  }

  public void visitCfmlComponent(CfmlComponent component) {
    visitElement(component);
  }

  public void visitCfmlTag(CfmlTag tag) {
    visitElement(tag);
  }

  @Override
  public void visitFile(@NotNull PsiFile file) {
    if (file instanceof CfmlFile) {
      file.acceptChildren(this);
    }
  }
}
