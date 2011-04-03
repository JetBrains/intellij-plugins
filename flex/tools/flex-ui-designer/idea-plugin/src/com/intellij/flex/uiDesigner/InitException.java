package com.intellij.flex.uiDesigner;

import org.jetbrains.annotations.PropertyKey;

class InitException extends Exception {
  public InitException(@PropertyKey(resourceBundle = FlexUIDesignerBundle.BUNDLE) String key, Object... params) {
    super(FlexUIDesignerBundle.message(key, params));
  }
}
