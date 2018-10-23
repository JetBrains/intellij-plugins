// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.css.StylesheetFile;
import com.intellij.psi.css.resolve.CssInclusionContext;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Angular2CssInclusionContext extends CssInclusionContext {
  @NotNull
  @Override
  public PsiFile[] getContextFiles(@NotNull PsiFile current) {
    final PsiElement context = current.getContext();
    final JSProperty property = PsiTreeUtil.getParentOfType(context, JSProperty.class);
    if (property != null && "template".equals(property.getName())) {
      final JSObjectLiteralExpression object = (JSObjectLiteralExpression)property.getParent();
      final JSProperty styles = object.findProperty("styles");
      if (styles != null && styles.getValue() instanceof JSArrayLiteralExpression) {
        final List<PsiFile> result = new SmartList<>();
        for (JSExpression expression : ((JSArrayLiteralExpression)styles.getValue()).getExpressions()) {
          if (expression instanceof JSLiteralExpression && ((JSLiteralExpression)expression).isQuotedLiteral()) {
            final List<Pair<PsiElement, TextRange>> injected = InjectedLanguageManager.getInstance(context.getProject()).getInjectedPsiFiles(expression);
            if (injected != null) {
              for (Pair<PsiElement, TextRange> pair : injected) {
                if (pair.first instanceof StylesheetFile) {
                  result.add((PsiFile)pair.first);
                }
              }
            }
          }
        }
        return result.toArray(PsiFile.EMPTY_ARRAY);
      }
    }
    return PsiFile.EMPTY_ARRAY;
  }
}
