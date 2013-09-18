package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ui.configuration.artifacts.sourceItems.ModuleSourceItemGroup;
import com.intellij.packaging.artifacts.Artifact;
import com.intellij.packaging.ui.ArtifactEditorContext;
import com.intellij.packaging.ui.PackagingSourceItem;
import com.intellij.packaging.ui.PackagingSourceItemFilter;
import org.jetbrains.annotations.NotNull;

/**
 * @author nik
 */
public class FlexPackagingSourceItemFilter extends PackagingSourceItemFilter {
  @Override
  public boolean isAvailable(@NotNull PackagingSourceItem item, @NotNull ArtifactEditorContext context) {
    if (item instanceof ModuleSourceItemGroup &&
        ModuleType.get(((ModuleSourceItemGroup)item).getModule()) == FlexModuleType.getInstance()) {
      return false;
    }
    return true;
  }
}
