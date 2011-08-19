package com.intellij.lang.javascript.flex.projectStructure.options;

public class AirDesktopPackagingOptions extends AirPackagingOptions implements Cloneable {

  protected AirDesktopPackagingOptions clone() {
    try {
      return (AirDesktopPackagingOptions)super.clone();
    }
    catch (CloneNotSupportedException e) {
      assert false;
      return null;
    }
  }
}
