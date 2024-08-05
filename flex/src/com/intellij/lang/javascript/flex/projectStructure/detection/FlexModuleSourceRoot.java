// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.projectStructure.detection;

import com.intellij.ide.util.projectWizard.importSources.DetectedProjectRoot;
import com.intellij.ide.util.projectWizard.importSources.DetectedSourceRoot;
import com.intellij.lang.javascript.flex.FlexBundle;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class FlexModuleSourceRoot extends DetectedSourceRoot {

  protected FlexModuleSourceRoot(File directory) {
    super(directory, null);
  }

  @Override
  public @NotNull String getRootTypeName() {
    return FlexBundle.message("autodetected.source.root.type");
  }

  @Override
  public boolean canContainRoot(final @NotNull DetectedProjectRoot root) {
    return !(root instanceof FlexModuleSourceRoot);
  }

  @Override
  public DetectedProjectRoot combineWith(final @NotNull DetectedProjectRoot root) {
    if (root instanceof FlexModuleSourceRoot) {
      return this;
    }
    else {
      return null;
    }
  }
}
