package com.intellij.lang.javascript.flex.artifacts;

import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ui.configuration.artifacts.sourceItems.ModuleSourceItemGroup;
import com.intellij.packaging.ui.ArtifactEditorContext;
import com.intellij.packaging.ui.PackagingSourceItem;
import com.intellij.packaging.ui.PackagingSourceItemFilter;
import org.jetbrains.annotations.NotNull;

public class FlashPackagingSourceItemFilter extends PackagingSourceItemFilter {
  @Override
  public boolean isAvailable(@NotNull PackagingSourceItem item, @NotNull ArtifactEditorContext context) {
    if (item instanceof ModuleSourceItemGroup &&
        ModuleType.get(((ModuleSourceItemGroup)item).getModule()) == FlexModuleType.getInstance()) {
      return false;
    }
    return true;
  }
}
