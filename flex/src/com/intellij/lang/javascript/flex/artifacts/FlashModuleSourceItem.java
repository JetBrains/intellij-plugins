// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.artifacts;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.packaging.elements.PackagingElement;
import com.intellij.packaging.ui.ArtifactEditorContext;
import com.intellij.packaging.ui.PackagingSourceItem;
import com.intellij.packaging.ui.SourceItemPresentation;
import com.intellij.packaging.ui.SourceItemWeights;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class FlashModuleSourceItem extends PackagingSourceItem {
  private final Module myModule;

  public FlashModuleSourceItem(final Module module) {
    myModule = module;
  }

  @Override
  public @NotNull List<? extends PackagingElement<?>> createElements(@NotNull ArtifactEditorContext context) {
    return Collections.emptyList();
  }

  @Override
  public @NotNull SourceItemPresentation createPresentation(final @NotNull ArtifactEditorContext context) {
    return new SourceItemPresentation() {

      @Override
      public String getPresentableName() {
        return myModule.getName();
      }

      @Override
      public void render(final @NotNull PresentationData presentationData,
                         final SimpleTextAttributes mainAttributes,
                         final SimpleTextAttributes commentAttributes) {
        presentationData.setIcon(ModuleType.get(myModule).getIcon());
        presentationData.addText(myModule.getName(), mainAttributes);
      }

      @Override
      public int getWeight() {
        return SourceItemWeights.MODULE_WEIGHT - 1;
      }
    };
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof FlashModuleSourceItem && myModule.equals(((FlashModuleSourceItem)obj).myModule);
  }

  @Override
  public int hashCode() {
    return myModule.hashCode();
  }

  public Module getModule() {
    return myModule;
  }
}
