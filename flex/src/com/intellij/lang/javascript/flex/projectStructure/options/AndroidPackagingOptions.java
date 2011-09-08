package com.intellij.lang.javascript.flex.projectStructure.options;

import com.intellij.lang.javascript.flex.projectStructure.model.AirPackagingOptions;

public class AndroidPackagingOptions implements AirPackagingOptions, Cloneable {

  protected AndroidPackagingOptions clone() {
    try {
      return (AndroidPackagingOptions)super.clone();
    }
    catch (CloneNotSupportedException e) {
      assert false;
      return null;
    }
  }
}
