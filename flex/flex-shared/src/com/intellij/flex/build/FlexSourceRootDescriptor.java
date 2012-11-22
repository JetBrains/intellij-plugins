package com.intellij.flex.build;

import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildRootDescriptor;
import org.jetbrains.jps.builders.BuildTarget;
import org.jetbrains.jps.cmdline.ProjectDescriptor;

import java.io.File;
import java.io.FileFilter;

class FlexSourceRootDescriptor extends BuildRootDescriptor {
  private final File myRoot;
  private BuildTarget myTarget;

  public FlexSourceRootDescriptor(final BuildTarget target, final File root) {
    myTarget = target;
    myRoot = root;
  }

  @Override
  public String getRootId() {
    return FileUtil.toSystemIndependentName(myRoot.getAbsolutePath());
  }

  @Override
  public File getRootFile() {
    return myRoot;
  }

  @Override
  public BuildTarget<?> getTarget() {
    return myTarget;
  }

  @Override
  public FileFilter createFileFilter(@NotNull ProjectDescriptor descriptor) {
    return new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return true;
      }
    };
  }
}
