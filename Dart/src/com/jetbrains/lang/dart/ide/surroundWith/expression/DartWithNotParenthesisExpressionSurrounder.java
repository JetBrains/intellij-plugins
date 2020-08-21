// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.surroundWith.expression;

import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.DartBundle;

public class DartWithNotParenthesisExpressionSurrounder extends DartWithExpressionSurrounder {
  @Override
  protected String getTemplateText(PsiElement expr) {
    return "!(" + expr.getText() + ")";
  }

  @Override
  public String getTemplateDescription() {
    //noinspection DialogTitleCapitalization
    return DartBundle.message("dart.surround.with.not.parenthesis");
  }
}
