package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.lang.javascript.flex.projectStructure.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * User: ksafonov
 */
public class NonStructuralModifiableDependencies implements Dependencies {
  private final DependenciesImpl myOriginal;

  NonStructuralModifiableDependencies(final DependenciesImpl original) {
    myOriginal = original;
  }

  @Override
  public SdkEntry getSdkEntry() {
    return myOriginal.getSdkEntry();
  }

  @Override
  public DependencyEntry[] getEntries() {
    return myOriginal.getEntries();
  }

  @NotNull
  @Override
  public LinkageType getFrameworkLinkage() {
    return myOriginal.getFrameworkLinkage();
  }

  @NotNull
  @Override
  public String getTargetPlayer() {
    return myOriginal.getTargetPlayer();
  }

  @NotNull
  @Override
  public ComponentSet getComponentSet() {
    return myOriginal.getComponentSet();
  }
}
