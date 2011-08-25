package com.intellij.lang.javascript.flex;

import com.intellij.lang.javascript.flex.build.FlexCompilerSettingsEditor;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeUtils;
import com.intellij.lang.javascript.flex.projectStructure.ui.FlexModuleEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleConfigurationEditor;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ui.configuration.DefaultModuleConfigurationEditorFactory;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationEditorProvider;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState;
import com.intellij.peer.PeerFactory;
import com.intellij.util.PlatformUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Maxim.Mossienko
 */
public class FlexModuleConfigurationEditorProvider implements ModuleConfigurationEditorProvider {
  public ModuleConfigurationEditor[] createEditors(final ModuleConfigurationState state) {
    final Module module = state.getRootModel().getModule();
    if (ModuleType.get(module) != FlexModuleType.getInstance()) return ModuleConfigurationEditor.EMPTY;

    List<ModuleConfigurationEditor> result = new ArrayList<ModuleConfigurationEditor>();
    if (PlatformUtils.isFlexIde() && FlexIdeUtils.isNewUI()) {
      result.add(new FlexModuleEditor(state));
    }
    else {
      result.add(PeerFactory.getInstance().createModuleConfigurationEditor(module.getName(), state));
      result.add(DefaultModuleConfigurationEditorFactory.getInstance().createClasspathEditor(state));
      result.add(new FlexCompilerSettingsEditor(module, state.getRootModel().getModuleExtension(CompilerModuleExtension.class)));
    }
    return result.toArray(new ModuleConfigurationEditor[result.size()]);
  }
}
