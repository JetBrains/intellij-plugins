package com.intellij.lang.javascript.flex.library;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.ui.RootDetector;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
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

  private static void collectRoots(VirtualFile file, final List<VirtualFile> result, final ProgressIndicator progressIndicator) {
    if (!file.isDirectory() || file.getFileSystem() instanceof JarFileSystem) return;

    VfsUtilCore.visitChildrenRecursively(file, new VirtualFileVisitor() {
      @NotNull
      @Override
      public Result visitFileEx(@NotNull VirtualFile child) {
        progressIndicator.checkCanceled();
        if (child.isDirectory()) {
          progressIndicator.setText2(child.getPresentableUrl());
        }
        else if ("swc".equalsIgnoreCase(child.getExtension()) || "ane".equalsIgnoreCase(child.getExtension())) {
          final VirtualFile dir = child.getParent();
          result.add(dir);
          return skipTo(dir);
        }
        return CONTINUE;
      }
    });
  }
}
