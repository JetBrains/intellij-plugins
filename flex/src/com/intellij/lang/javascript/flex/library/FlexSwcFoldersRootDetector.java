package com.intellij.lang.javascript.flex.library;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.ui.RootDetector;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: ksafonov
 */
class FlexSwcFoldersRootDetector extends RootDetector {

  public FlexSwcFoldersRootDetector() {
    super(OrderRootType.CLASSES, true, FlexBundle.message("swc.folders.root.detector.name"));
  }

  @NotNull
  @Override
  public Collection<VirtualFile> detectRoots(@NotNull final VirtualFile rootCandidate, @NotNull final ProgressIndicator progressIndicator) {
    List<VirtualFile> result = new ArrayList<VirtualFile>();
    collectRoots(rootCandidate, result, progressIndicator);
    return result;
  }

  private static void collectRoots(VirtualFile file, List<VirtualFile> result, ProgressIndicator progressIndicator) {
    progressIndicator.checkCanceled();
    if (!file.isDirectory() || file.getFileSystem() instanceof JarFileSystem) return;

    progressIndicator.setText2(file.getPresentableUrl());

    for (VirtualFile child : file.getChildren()) {
      if (!child.isDirectory() && ("swc".equalsIgnoreCase(child.getExtension()) || "ane".equalsIgnoreCase(child.getExtension()))) {
        result.add(file);
        return;
      }
      collectRoots(child, result, progressIndicator);
    }
  }
}
