package com.intellij.javascript.karma.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Sergey Simonchik
 */
public class NodeInstalledPackage {

  private final String myName;
  private final File mySourceRootDir;

  public NodeInstalledPackage(@NotNull String name, @NotNull File sourceRootDir) {
    myName = name;
    mySourceRootDir = sourceRootDir;
  }

  @NotNull
  public String getName() {
    return myName;
  }

  @NotNull
  public File getSourceRootDir() {
    return mySourceRootDir;
  }

}
