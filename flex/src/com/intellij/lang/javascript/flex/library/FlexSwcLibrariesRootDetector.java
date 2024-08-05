// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.library;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.ui.RootFilter;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

class FlexSwcLibrariesRootDetector extends RootFilter {

  FlexSwcLibrariesRootDetector() {
    super(OrderRootType.CLASSES, false, FlexBundle.message("swc.libraries.root.detector.name"));
  }

  @Override
  public boolean isAccepted(final @NotNull VirtualFile rootCandidate, final @NotNull ProgressIndicator progressIndicator) {
    if (!rootCandidate.isDirectory()){
      return false;
    }
    if (!"swc".equalsIgnoreCase(rootCandidate.getExtension()) && !"ane".equalsIgnoreCase(rootCandidate.getExtension())) {
      return false;
    }
    if (!(rootCandidate.getFileSystem() instanceof JarFileSystem) || rootCandidate.getParent() != null) {
      return false;
    }
    return true;
  }
}
