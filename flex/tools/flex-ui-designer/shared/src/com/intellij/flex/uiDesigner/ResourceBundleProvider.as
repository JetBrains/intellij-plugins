package com.intellij.flex.uiDesigner {
import flash.utils.Dictionary;

public interface ResourceBundleProvider {
  [Nullable]
  function getResourceBundle(locale:String, bundleName:String):Dictionary;
}
}
