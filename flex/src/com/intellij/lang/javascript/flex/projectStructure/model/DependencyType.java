package com.intellij.lang.javascript.flex.projectStructure.model;

import com.intellij.flex.model.bc.LinkageType;
import org.jetbrains.annotations.NotNull;

public interface DependencyType {

  LinkageType DEFAULT_LINKAGE = LinkageType.Merged;

  @NotNull
  LinkageType getLinkageType();

  boolean isEqual(DependencyType other);
}
