package com.intellij.lang.javascript.flex.projectStructure.options;

import com.intellij.lang.javascript.flex.projectStructure.model.AirPackagingOptions;

public class IOSPackagingOptions implements AirPackagingOptions, Cloneable {

  protected IOSPackagingOptions clone() {
    try {
      return (IOSPackagingOptions)super.clone();
    }
    catch (CloneNotSupportedException e) {
      assert false;
      return null;
    }
  }
}
