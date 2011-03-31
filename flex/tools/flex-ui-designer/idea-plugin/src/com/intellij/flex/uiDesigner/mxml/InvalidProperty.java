package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.FlexUIDesignerBundle;
import org.jetbrains.annotations.PropertyKey;

class InvalidProperty extends Exception {
  InvalidProperty(String message) {
    super(message);
  }
  
  InvalidProperty(@PropertyKey(resourceBundle = FlexUIDesignerBundle.BUNDLE) String key, Object... params) {
    super(FlexUIDesignerBundle.message(key, params));
  }
}
