package com.intellij.flex.uiDesigner;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public class InvalidPropertyException extends Exception {
  public InvalidPropertyException(@PropertyKey(resourceBundle = FlexUIDesignerBundle.BUNDLE) String key, Object... params) {
    super(FlexUIDesignerBundle.message(key, params));
  }

  public InvalidPropertyException(@NotNull Throwable e, @PropertyKey(resourceBundle = FlexUIDesignerBundle.BUNDLE) String key, Object... params) {
    super(FlexUIDesignerBundle.message(key, params), e);
  }
}