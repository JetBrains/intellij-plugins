package com.intellij.lang.javascript.flex;

import com.intellij.lang.javascript.flex.build.FlexCompilerSettingsEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleConfigurationEditor;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ui.configuration.DefaultModuleConfigurationEditorFactory;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationEditorProvider;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState;
import com.intellij.peer.PeerFactory;

/**
 * @author Maxim.Mossienko
 */
public class FlexModuleConfigurationEditorProvider implements ModuleConfigurationEditorProvider {
  public ModuleConfigurationEditor[] createEditors(final ModuleConfigurationState state) {
    final Module module = state.getRootModel().getModule();
    if (module.getModuleType() != FlexModuleType.getInstance()) return ModuleConfigurationEditor.EMPTY;
    return new ModuleConfigurationEditor[] {
      PeerFactory.getInstance().createModuleConfigurationEditor(
        module.getName(), state
      ),
      DefaultModuleConfigurationEditorFactory.getInstance().createClasspathEditor(state),
      new FlexCompilerSettingsEditor(state.getRootModel().getModule(),
                                     state.getRootModel().getModuleExtension(CompilerModuleExtension.class))};
  }

}
