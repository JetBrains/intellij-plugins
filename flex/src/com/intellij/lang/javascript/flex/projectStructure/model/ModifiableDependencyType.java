package com.intellij.lang.javascript.flex.projectStructure.model;

import com.intellij.flex.model.bc.LinkageType;
import org.jetbrains.annotations.NotNull;

/**
 * User: ksafonov
 */
public interface ModifiableDependencyType extends DependencyType {

  void setLinkageType(@NotNull LinkageType linkageType);

  void copyFrom(DependencyType dependencyType);
}
