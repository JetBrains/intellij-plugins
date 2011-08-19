package com.intellij.lang.javascript.flex.projectStructure.options;

public class IOSPackagingOptions extends AirPackagingOptions implements Cloneable {

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
