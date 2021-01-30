package com.intellij.lang.javascript.flex;

import com.intellij.lang.javascript.flex.projectStructure.ui.FlexModuleEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleConfigurationEditor;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationEditorProvider;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState;

/**
 * @author Maxim.Mossienko
 */
public class FlexModuleConfigurationEditorProvider implements ModuleConfigurationEditorProvider {
  @Override
  public ModuleConfigurationEditor[] createEditors(final ModuleConfigurationState state) {
    final Module module = state.getCurrentRootModel().getModule();
    if (ModuleType.get(module) != FlexModuleType.getInstance()) return ModuleConfigurationEditor.EMPTY;
    return new ModuleConfigurationEditor[]{new FlexModuleEditor(state)};
  }

}
