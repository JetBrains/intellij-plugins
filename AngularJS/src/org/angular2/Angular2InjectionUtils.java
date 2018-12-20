// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Angular2InjectionUtils {

  public static PsiFile getFirstInjectedFile(@Nullable JSExpression expression) {
    if (expression != null) {
      List<Pair<PsiElement, TextRange>> injections =
        InjectedLanguageManager.getInstance(expression.getProject()).getInjectedPsiFiles(expression);
      if (injections != null) {
        for (Pair<PsiElement, TextRange> injection : injections) {
          if (injection.getFirst() instanceof PsiFile) {
            return (PsiFile)injection.getFirst();
          }
        }
      }
    }
    return null;
  }
}
