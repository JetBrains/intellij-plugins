package com.intellij.lang.javascript.flex.build;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.List;

public interface FlexCompilationTask {

  void start(final FlexCompilationManager compilationManager);

  void cancel();

  boolean isFinished();

  boolean isCompilationFailed();

  String getPresentableName();

  FlexBuildConfiguration getConfig();

  Module getModule();

  List<VirtualFile> getConfigFiles();
}
