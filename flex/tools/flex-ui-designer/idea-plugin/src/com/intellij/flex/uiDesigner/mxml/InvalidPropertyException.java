package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.FlexUIDesignerBundle;
import org.jetbrains.annotations.PropertyKey;

class InvalidPropertyException extends Exception {  
  InvalidPropertyException(@PropertyKey(resourceBundle = FlexUIDesignerBundle.BUNDLE) String key, Object... params) {
    super(FlexUIDesignerBundle.message(key, params));
  }
}
