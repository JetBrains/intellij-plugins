package com.intellij.lang.javascript.flex.projectStructure.model;

import com.intellij.lang.javascript.flex.projectStructure.model.DependencyType;
import com.intellij.lang.javascript.flex.projectStructure.model.LinkageType;
import org.jetbrains.annotations.NotNull;

/**
 * User: ksafonov
 */
public interface ModifiableDependencyType extends DependencyType {

  void setLinkageType(@NotNull LinkageType linkageType);

  void copyFrom(DependencyType dependencyType);
}
