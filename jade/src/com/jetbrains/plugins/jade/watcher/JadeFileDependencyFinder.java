package com.jetbrains.plugins.jade.watcher;

import com.intellij.plugins.watcher.config.FileDependencyFinder;
import com.jetbrains.plugins.jade.JadeToPugTransitionHelper;
import org.jetbrains.annotations.Nullable;

final class JadeFileDependencyFinder extends FileDependencyFinder {
  @Override
  public boolean accept(@Nullable String fileExtension) {
    return fileExtension != null && JadeToPugTransitionHelper.ALL_EXTENSIONS.contains(fileExtension);
  }

  @Override
  public boolean updateGeneratedFilesOfDependencies() {
    return true;
  }

}
