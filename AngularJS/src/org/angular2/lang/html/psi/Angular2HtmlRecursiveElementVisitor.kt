// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveVisitor;
import org.jetbrains.annotations.NotNull;

public class Angular2HtmlRecursiveElementVisitor extends Angular2HtmlElementVisitor implements PsiRecursiveVisitor {
  @Override
  public void visitElement(@NotNull PsiElement element) {
    element.acceptChildren(this);
  }
}