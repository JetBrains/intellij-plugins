// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart;

import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import org.jetbrains.annotations.NotNull;

public class DartModuleRootListener implements ModuleRootListener {
  @Override
  public void rootsChanged(@NotNull ModuleRootEvent event) {
    DartFileListener.scheduleDartPackageRootsUpdate(event.getProject());
  }
}
