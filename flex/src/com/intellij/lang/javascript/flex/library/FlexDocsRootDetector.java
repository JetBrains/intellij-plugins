package com.intellij.lang.javascript.flex.library;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.libraries.ui.RootDetector;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author ksafonov
 */
class FlexDocsRootDetector extends RootDetector {
  public FlexDocsRootDetector() {
    super(JavadocOrderRootType.getInstance(), false, FlexBundle.message("docs.root.detector.name"));
  }

  @NotNull
  @Override
  public Collection<VirtualFile> detectRoots(@NotNull final VirtualFile rootCandidate, @NotNull final ProgressIndicator progressIndicator) {
    List<VirtualFile> result = new ArrayList<VirtualFile>();
    collectRoots(rootCandidate, result, progressIndicator);
    return result;
  }

  private static void collectRoots(VirtualFile file, final List<VirtualFile> result, final ProgressIndicator progressIndicator) {
    VfsUtilCore.visitChildrenRecursively(file, new VirtualFileVisitor() {
      @Override
      public boolean visitFile(@NotNull VirtualFile file) {
        progressIndicator.checkCanceled();
        if (!file.isDirectory()) return false;
        progressIndicator.setText2(file.getPresentableUrl());

        if (file.findChild("all-classes.html") != null) {
          result.add(file);
          return false;
        }

        return true;
      }
    });
  }
}
