package com.intellij.lang.javascript.flex.projectStructure.options;

public class HtmlWrapperOptions implements Cloneable {

  protected HtmlWrapperOptions clone() {
    try {
      return (HtmlWrapperOptions)super.clone();
    }
    catch (CloneNotSupportedException e) {
      assert false;
      return null;
    }
  }
}
