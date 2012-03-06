package com.intellij.lang.javascript.flex.library;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.libraries.ui.RootDetector;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: ksafonov
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

  private static void collectRoots(VirtualFile file, List<VirtualFile> result, ProgressIndicator progressIndicator) {
    progressIndicator.checkCanceled();
    if (!file.isDirectory()) return;

    progressIndicator.setText2(file.getPresentableUrl());

    if (file.findChild("all-classes.html") != null) {
      result.add(file);
      return;
    }
    for (VirtualFile child : file.getChildren()) {
      collectRoots(child, result, progressIndicator);
    }
  }
}
