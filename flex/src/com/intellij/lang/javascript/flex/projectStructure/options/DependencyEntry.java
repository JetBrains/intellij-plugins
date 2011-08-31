package com.intellij.lang.javascript.flex.projectStructure.options;

/**
 * @author ksafonov
 */
public abstract class DependencyEntry {

  protected final DependencyType myDependencyType = new DependencyType();

  public DependencyType getDependencyType() {
    return myDependencyType;
  }

  public abstract DependencyEntry getCopy();

  public void applyTo(DependencyEntry other) {
    myDependencyType.applyTo(other.myDependencyType);
  }
}

