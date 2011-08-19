package com.intellij.lang.javascript.flex.projectStructure.options;

public class Dependencies implements Cloneable {

  protected Dependencies clone() {
    try {
      return (Dependencies)super.clone();
    }
    catch (CloneNotSupportedException e) {
      assert false;
      return null;
    }
  }
}
