package com.intellij.flexIde;

import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.ui.FlexModuleEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleConfigurationEditor;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationEditorProvider;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState;

/**
 * @author ksafonov
 */
public class FlexIdeModuleConfigurationEditorProvider implements ModuleConfigurationEditorProvider {
  public ModuleConfigurationEditor[] createEditors(final ModuleConfigurationState state) {
    Module module = state.getRootModel().getModule();
    if (ModuleType.get(module) != FlexModuleType.getInstance()) {
      return ModuleConfigurationEditor.EMPTY;
    }
    return new ModuleConfigurationEditor[]{new FlexModuleEditor(state)};
  }
}
