// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex;

import com.intellij.lang.javascript.flex.projectStructure.ui.FlexModuleEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleConfigurationEditor;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationEditorProvider;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState;
import org.jetbrains.annotations.NotNull;

final class FlexModuleConfigurationEditorProvider implements ModuleConfigurationEditorProvider {
  @Override
  public @NotNull ModuleConfigurationEditor @NotNull [] createEditors(@NotNull ModuleConfigurationState state) {
    Module module = state.getCurrentRootModel().getModule();
    if (ModuleType.get(module) != FlexModuleType.getInstance()) {
      return ModuleConfigurationEditor.EMPTY;
    }
    return new ModuleConfigurationEditor[]{new FlexModuleEditor(state)};
  }
}
