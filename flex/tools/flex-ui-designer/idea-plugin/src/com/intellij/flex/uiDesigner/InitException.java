package com.intellij.flex.uiDesigner;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

class InitException extends Exception {
  public InitException(@NotNull Throwable e, @PropertyKey(resourceBundle = FlexUIDesignerBundle.BUNDLE) String key, Object... params) {
    super(FlexUIDesignerBundle.message(key, params), e);
  }
}
