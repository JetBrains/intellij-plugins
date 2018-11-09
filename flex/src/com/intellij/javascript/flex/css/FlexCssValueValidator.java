package com.intellij.javascript.flex.css;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssTerm;
import com.intellij.psi.css.CssTermType;
import com.intellij.psi.css.descriptor.value.CssValueDescriptor;
import com.intellij.psi.css.impl.CssTermTypes;
import com.intellij.psi.css.impl.descriptor.value.CssColorValue;
import com.intellij.psi.css.impl.descriptor.value.CssLengthValue;
import com.intellij.psi.css.impl.descriptor.value.CssStringValue;
import com.intellij.psi.css.impl.descriptor.value.CssValueValidatorImpl;
import com.intellij.xml.util.ColorMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

class FlexCssValueValidator extends CssValueValidatorImpl {
  FlexCssValueValidator(@NotNull FlexCssElementDescriptorProvider provider) {
    super(provider);
  }

  @Override
  public boolean isValid(@Nullable PsiElement term, @NotNull CssValueDescriptor valueDescriptor) {
    if (valueDescriptor instanceof CssColorValue) {
      return term != null && isValidColor(term);
    }
    else if (valueDescriptor instanceof CssStringValue) {
      return term != null && isValidString(term) && super.isValid(term, valueDescriptor);
    }
    else if (valueDescriptor instanceof CssLengthValue) {
      return term instanceof CssTerm &&
             (super.isValid(term, valueDescriptor) || isValidLength((CssTerm)term));
    }

    return super.isValid(term, valueDescriptor);
  }


  private static boolean isValidString(@NotNull PsiElement element) {
    return FlexCssUtil.inQuotes(element.getText().trim());
  }
  
  private static boolean isValidLength(@NotNull CssTerm cssTerm) {
    final CssTermType termType = cssTerm.getTermType();
    return termType == CssTermTypes.NEGATIVE_NUMBER  || termType == CssTermTypes.NUMBER || termType == CssTermTypes.INTEGER;
  }

  private static boolean isValidColor(@NotNull PsiElement element) {
    String text = element.getText().trim();
    if (isInteger(text)) {
      return true;
    }

    if (!(element instanceof CssTerm) || ((CssTerm)element).getTermType() != CssTermTypes.COLOR) {
      if (FlexCssUtil.inQuotes(text)) {
        text = StringUtil.unquoteString(text, '"');
        if (text.startsWith("0x")) {
          return isInteger(text.substring(2));
        }
        else if (containsOnlyLetters(text)) {
          return ColorMap.isStandardColor(text.toLowerCase(Locale.US));
        }
      }
      return false;
    }

    if (containsOnlyLetters(text)) {
      return ColorMap.isStandardColor(text.toLowerCase(Locale.US));
    }
    return true;
  }

  private static boolean containsOnlyLetters(@NotNull String s) {
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
}
