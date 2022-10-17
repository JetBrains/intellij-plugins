// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.util;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.plugins.drools.lang.psi.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DroolsCommonUtil {

  public static boolean isMvelDialect(@NotNull PsiElement place) {
    final PsiFile file = place.getContainingFile();
    final String dialect = "dialect";
    if (file instanceof DroolsFile) {
      final DroolsAttribute attribute = ((DroolsFile)file).findAttributeByName(dialect);
      if (attribute != null) {
        return isMvelExpression(attribute);
      }
    }

    final DroolsRuleStatement ruleStatement = PsiTreeUtil.getParentOfType(place, DroolsRuleStatement.class, false);
    if (ruleStatement != null) {
      final DroolsRuleAttributes droolsRuleAttributes = ruleStatement.getRuleAttributes();
      if (droolsRuleAttributes != null) {
        for (DroolsAttribute attribute : droolsRuleAttributes.getAttributeList()) {
          if (dialect.equals(attribute.getAttributeName())) {
            return isMvelExpression(attribute);
          }
        }
      }
    }
    return false;
  }

  private static boolean isMvelExpression(DroolsAttribute attribute) {
    final DroolsExpression expression = attribute.getExpression();

    return expression instanceof DroolsStringLiteral && "mvel".equals(getInnerText((DroolsStringLiteral)expression));
  }

  @Nullable
  private static String getInnerText(@NotNull DroolsStringLiteral stringExpression) {
    String text = stringExpression.getText();
    int textLength = text.length();
    if (StringUtil.endsWithChar(text, '\"')) {
      if (textLength == 1) return null;
      text = text.substring(1, textLength - 1);
    }
    else {
      if (text.startsWith("&quot;") && text.endsWith("&quot;") && textLength > "&quot;".length()) {
        text = text.substring("&quot;".length(), textLength - "&quot;".length());
      }
      else {
        return null;
      }
    }
    return text;
  }
}
