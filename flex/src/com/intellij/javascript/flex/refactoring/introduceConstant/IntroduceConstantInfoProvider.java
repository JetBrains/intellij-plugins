// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.refactoring.introduceConstant;

import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.refactoring.introduce.BasicIntroducedEntityInfoProvider;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.NameUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ArrayUtilRt;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;

public class IntroduceConstantInfoProvider extends BasicIntroducedEntityInfoProvider {
  public IntroduceConstantInfoProvider(JSExpression mainOccurrence, JSExpression[] occurrences, PsiElement scope) {
    super(mainOccurrence, occurrences, scope);
  }

  @Override
  public String[] suggestCandidateNames() {
    if (myMainOccurrence instanceof JSLiteralExpression) {
      String text = StringUtil.unquoteString(myMainOccurrence.getText());
      LinkedHashSet<String> names = new LinkedHashSet<>(NameUtil.getSuggestionsByName(text, "", "", true, true, false));
      return ArrayUtil.mergeArrays(ArrayUtilRt.toStringArray(names), super.suggestCandidateNames());
    }
    return super.suggestCandidateNames();
  }

  @Override
  public boolean checkConflicts(@NotNull String name) {
    return true;
  }

  @Override
  protected boolean processStatics(PsiElement place) {
    return true;
  }
}
