package com.intellij.flex.uiDesigner {
import flash.utils.Dictionary;

public interface ResourceBundleProvider {
  [Nullable]
  function getResourceBundle(project:Object, locale:String, bundleName:String):Dictionary;
}
}
