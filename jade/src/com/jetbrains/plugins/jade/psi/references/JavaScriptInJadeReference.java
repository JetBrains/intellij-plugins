// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.psi.references;

import com.intellij.lang.javascript.psi.JSNamedElement;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class JavaScriptInJadeReference extends PsiPolyVariantReferenceBase<JSNamedElement> {

  public JavaScriptInJadeReference(JSNamedElement element, TextRange range) {
    super(element, range);
  }

  @Override
  public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
    String name = myElement.getName();
    if (name == null) {
      return ResolveResult.EMPTY_ARRAY;
    }

    PsiFile file = myElement.getContainingFile();

    List<ResolveResult> results = new ArrayList<>();
    file.acceptChildren(new PsiRecursiveElementVisitor() {
      @Override
      public void visitElement(@NotNull PsiElement el) {
        if (!(el instanceof JSNamedElement ref)) {
          super.visitElement(el);
          return;
        }

        if (name.equals(ref.getName())) {
          results.add(new PsiElementResolveResult(el));
        }
      }
    });

    return results.toArray(ResolveResult.EMPTY_ARRAY);
  }
}
