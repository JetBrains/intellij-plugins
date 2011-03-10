package com.intellij.javascript.flex.css;

import com.intellij.psi.PsiElement;
import com.intellij.psi.css.impl.util.table.CssColorValue;
import com.intellij.xml.util.ColorSampleLookupValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Eugene.Kudelevsky
 */
public class FlexCssColorValue extends CssColorValue {
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
          return ColorSampleLookupValue.isStandardColor(text.toLowerCase());
        }
      }
      return false;
    }
    if (containsOnlyLetters(text)) {
      return ColorSampleLookupValue.isStandardColor(text.toLowerCase());
    }
    return true;
  }
}
