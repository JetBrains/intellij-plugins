package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.flex.ResourceBundle;

public interface ResourceBundleProvider {
  [Nullable]
  function getResourceBundle(locale:String, bundleName:String, bundleClass:Class):ResourceBundle;
}
}