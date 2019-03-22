// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SyntaxTraverser;
import com.intellij.psi.impl.PsiTreeChangeEventImpl;
import com.intellij.psi.impl.PsiTreeChangePreprocessorBase;
import com.intellij.psi.xml.XmlFile;
import com.jetbrains.lang.dart.psi.DartEmbeddedContent;
import com.jetbrains.lang.dart.psi.DartFile;
import com.jetbrains.lang.dart.psi.DartPsiCompositeElement;
import com.jetbrains.lang.dart.psi.IDartBlock;
import org.jetbrains.annotations.NotNull;

public class DartPsiTreeChangePreprocessor extends PsiTreeChangePreprocessorBase {
  public DartPsiTreeChangePreprocessor(@NotNull Project project) {
    super(project);
  }

  @Override
  protected boolean acceptsEvent(@NotNull PsiTreeChangeEventImpl event) {
    return event.getFile() instanceof DartFile || event.getFile() instanceof XmlFile;
  }

  @Override
  protected boolean isOutOfCodeBlock(@NotNull PsiElement element) {
    boolean result = false;
    for (PsiElement p : SyntaxTraverser.psiApi().parents(element)) {
      if (p instanceof IDartBlock) return false;
      if (p instanceof DartEmbeddedContent) break;
      if (p instanceof DartPsiCompositeElement) result = true;
    }
    return result;
  }
}
