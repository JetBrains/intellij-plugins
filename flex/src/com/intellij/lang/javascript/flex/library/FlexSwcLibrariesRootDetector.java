package com.intellij.lang.javascript.flex.library;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.ui.RootFilter;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * User: ksafonov
 */
class FlexSwcLibrariesRootDetector extends RootFilter {

  public FlexSwcLibrariesRootDetector() {
    super(OrderRootType.CLASSES, false, FlexBundle.message("swc.libraries.root.detector.name"));
  }

  @Override
  public boolean isAccepted(@NotNull final VirtualFile rootCandidate, @NotNull final ProgressIndicator progressIndicator) {
    if (!rootCandidate.isDirectory()){
      return false;
    }
    if (!"swc".equalsIgnoreCase(rootCandidate.getExtension())) {
      return false;
    }
    if (!(rootCandidate.getFileSystem() instanceof JarFileSystem) || rootCandidate.getParent() != null) {
      return false;
    }
    return true;
  }
}
