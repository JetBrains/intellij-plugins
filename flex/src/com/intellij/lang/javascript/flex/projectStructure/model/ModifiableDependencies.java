package com.intellij.lang.javascript.flex.projectStructure.model;

import com.intellij.flex.model.bc.ComponentSet;
import com.intellij.flex.model.bc.LinkageType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * User: ksafonov
 */
public interface ModifiableDependencies extends Dependencies {

  List<ModifiableDependencyEntry> getModifiableEntries();

  void setFrameworkLinkage(@NotNull LinkageType frameworkLinkage);

  void setTargetPlayer(@NotNull String targetPlayer);

  void setComponentSet(@NotNull ComponentSet componentSet);

  void setSdkEntry(@Nullable SdkEntry sdk);

}
