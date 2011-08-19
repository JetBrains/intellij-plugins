package com.intellij.lang.javascript.flex.projectStructure.options;

public class AirDescriptorOptions implements Cloneable {

  protected AirDescriptorOptions clone() {
    try {
      return (AirDescriptorOptions)super.clone();
    }
    catch (CloneNotSupportedException e) {
      assert false;
      return null;
    }
  }
}
