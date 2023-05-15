package com.intellij.javascript.flex.css;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.impl.util.table.CssColorValue;
import com.intellij.xml.util.ColorMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FlexCssColorValue extends CssColorValue {
  public FlexCssColorValue() {
    super(false);
  }

  private static boolean containsOnlyLetters(String s) {
    for (int i = 0, n = s.length(); i < n; i++) {
      char c = s.charAt(i);
      if (!Character.isLetter(c)) {
        return false;
      }
    }
    return true;
  }

  private static boolean isInteger(@NotNull String s) {
    try {
      Integer.parseInt(s, 16);
    }
    catch (NumberFormatException e) {
      return false;
    }
    return true;
  }

  @Override
  public boolean isValueBelongs(@Nullable PsiElement element) {
    if (element == null) {
      return false;
    }
    String text = element.getText().trim();
    if (isInteger(text)) {
      return true;
    }
    if (!super.isValueBelongs(element)) {
      if (FlexCssUtil.inQuotes(text)) {
        text = text.substring(1, text.length() - 1);
        if (text.startsWith("0x")) {
          return isInteger(text.substring(2));
        }
        else if (containsOnlyLetters(text)) {
          return ColorMap.isStandardColor(StringUtil.toLowerCase(text));
        }
      }
      return false;
    }
    if (containsOnlyLetters(text)) {
      return ColorMap.isStandardColor(StringUtil.toLowerCase(text));
    }
    return true;
  }
}
