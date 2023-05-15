package com.intellij.javascript.flex.css;

import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssPropertyValue;
import com.intellij.psi.css.impl.util.table.CssPropertyValueImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FlexCssPropertyValue extends CssPropertyValueImpl {
  private final boolean mySoft;

  public FlexCssPropertyValue(boolean shorthand, boolean soft) {
    super(Type.OR);
    mySoft = soft;
    if (shorthand) {
      setMaxCount(-1);
      setMinCount(1);
    }
  }

  public FlexCssPropertyValue(@NotNull String value) {
    super(value);
    mySoft = false;
  }

  @Override
  public boolean isValueBelongs(@Nullable PsiElement element) {
    if (mySoft) return true;
    if (element == null) return false;
    if (isGroup()) {
      boolean belongs = false;
      for (CssPropertyValue each : getChildren()) {
        belongs |= each.isValueBelongs(element);
      }
      return belongs;
    }
    final String text = element.getText();
    assert text != null;
    return text.equals(getValue());
  }
}
