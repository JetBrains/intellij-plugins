package com.intellij.lang.javascript.flex.projectStructure.options;

public class AndroidPackagingOptions extends AirPackagingOptions implements Cloneable {

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
