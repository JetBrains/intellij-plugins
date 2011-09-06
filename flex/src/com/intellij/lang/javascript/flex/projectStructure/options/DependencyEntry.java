package com.intellij.lang.javascript.flex.projectStructure.options;

import org.jetbrains.annotations.NotNull;

/**
 * @author ksafonov
 */
public abstract class DependencyEntry {

  protected final DependencyType myDependencyType = new DependencyType();

  @NotNull
  public DependencyType getDependencyType() {
    return myDependencyType;
  }

  public abstract DependencyEntry getCopy();

}

