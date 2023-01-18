package com.intellij.javascript.flex.css;

import com.intellij.psi.PsiElement;
import com.intellij.psi.css.impl.util.table.CssPropertyValueImpl;
import org.jetbrains.annotations.Nullable;

public class FlexStringPropertyValue extends CssPropertyValueImpl {
  public FlexStringPropertyValue() {
    super(Type.OR);
  }

  @Override
  public boolean isValueBelongs(@Nullable PsiElement element) {
    if (element == null) {
      return false;
    }
    final String text = element.getText();
    return FlexCssUtil.inQuotes(text);
  }
}
