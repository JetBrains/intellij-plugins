package com.intellij.lang.javascript.flex.projectStructure.detection;

import com.intellij.ide.util.projectWizard.importSources.DetectedProjectRoot;
import com.intellij.ide.util.projectWizard.importSources.DetectedSourceRoot;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class FlexModuleSourceRoot extends DetectedSourceRoot {

  protected FlexModuleSourceRoot(File directory) {
    super(directory, null);
  }

  @NotNull
  @Override
  public String getRootTypeName() {
    return "Flex";
  }

  public boolean canContainRoot(@NotNull final DetectedProjectRoot root) {
    return !(root instanceof FlexModuleSourceRoot);
  }

  public DetectedProjectRoot combineWith(@NotNull final DetectedProjectRoot root) {
    if (root instanceof FlexModuleSourceRoot) {
      return this;
    }
    else {
      return null;
    }
  }
}
