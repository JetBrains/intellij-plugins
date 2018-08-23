// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.psi.JSExpressionStatement;
import com.intellij.lang.javascript.psi.JSInheritedLanguagesConfigurableProvider;
import com.intellij.lang.javascript.psi.JSParenthesizedExpression;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public class Angular2LanguageConfigurableProvider extends JSInheritedLanguagesConfigurableProvider {
  @Override
  public boolean isNeedToBeTerminated(@NotNull PsiElement element) {
    return false;
  }

  @Nullable
  @Override
  protected PsiElement createExpressionFromText(@NotNull String text,
                                                @NotNull PsiElement element) {
    JSExpressionStatement created =
      JSChangeUtil.createStatementPsiFromTextWithContext("(" + text + ")", element, JSExpressionStatement.class);
    JSParenthesizedExpression parenthesized = ObjectUtils.tryCast(created != null 
                                                                  ? created.getExpression() 
                                                                  : null, JSParenthesizedExpression.class);
    return parenthesized != null ? parenthesized.getInnerExpression() : null;
  }
}
