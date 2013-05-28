package com.jetbrains.lang.dart.ide.projectStructure;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleConfigurationEditor;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ui.configuration.ClasspathEditor;
import com.intellij.openapi.roots.ui.configuration.CommonContentEntriesEditor;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationEditorProvider;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState;
import com.jetbrains.lang.dart.ide.module.DartModuleType;

/**
 * @author: Fedor.Korotkov
 */
public class DartModuleConfigurationEditorProvider implements ModuleConfigurationEditorProvider {
  public ModuleConfigurationEditor[] createEditors(final ModuleConfigurationState state) {
    final Module module = state.getRootModel().getModule();
    if (ModuleType.get(module) != DartModuleType.getInstance()) {
      return ModuleConfigurationEditor.EMPTY;
    }
    return new ModuleConfigurationEditor[]{
      new CommonContentEntriesEditor(module.getName(), state, true, true),
      new ClasspathEditor(state)
    };
  }
}
