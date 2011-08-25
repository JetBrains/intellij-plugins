package com.intellij.lang.javascript.flex.projectStructure.options;

public class Dependencies implements Cloneable {

  public FlexIdeBuildConfiguration.ComponentSet COMPONENT_SET = FlexIdeBuildConfiguration.ComponentSet.SparkAndMx;
  public FlexIdeBuildConfiguration.FrameworkLinkage FRAMEWORK_LINKAGE = FlexIdeBuildConfiguration.FrameworkLinkage.Default;

  protected Dependencies clone() {
    try {
      Dependencies clone = (Dependencies)super.clone();
      clone.COMPONENT_SET = COMPONENT_SET;
      clone.FRAMEWORK_LINKAGE = FRAMEWORK_LINKAGE;
      return clone;
    }
    catch (CloneNotSupportedException e) {
      assert false;
      return null;
    }
  }
}
