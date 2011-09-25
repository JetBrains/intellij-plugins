package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleConfigurationEditor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.CommonContentEntriesEditor;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ModuleStructureConfigurable;
import org.jetbrains.annotations.Nls;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * @author ksafonov
 */
public class FlexModuleEditor implements ModuleConfigurationEditor {
  public static final String DISPLAY_NAME = "Flex";

  private final CommonContentEntriesEditor myEntriesEditor;
  private final Module myModule;

  public FlexModuleEditor(ModuleConfigurationState state) {
    myModule = state.getRootModel().getModule();
    myEntriesEditor = new CommonContentEntriesEditor(myModule.getName(), state, true, true);
    myEntriesEditor.getComponent().setBorder(new EmptyBorder(0, 0, 0, 0));
  }

  @Nls
  @Override
  public String getDisplayName() {
    return DISPLAY_NAME;
  }

  @Override
  public Icon getIcon() {
    return null;
  }

  @Override
  public String getHelpTopic() {
    return null; // TODO
  }

  @Override
  public JComponent createComponent() {
    return myEntriesEditor.createComponent();
  }

  //public static FlexModuleEditor getInstance(Module module) {
  //  ModuleEditor e =
  //    ModuleStructureConfigurable.getInstance(module.getProject()).getContext().getModulesConfigurator().getModuleEditor(module);
  //  e.getPanel(); // create editors
  //  return (FlexModuleEditor)e.getEditor(DISPLAY_NAME);
  //}


  @Override
  public boolean isModified() {
    if (myEntriesEditor.isModified()) return true;
    //ModifiableRootModel modifiableRootModel =
    //  ModuleStructureConfigurable.getInstance(myModule.getProject()).getContext().getModulesConfigurator().getOrCreateModuleEditor(myModule)
    //    .getModifiableRootModelProxy();
    //if (modifiableRootModel.isChanged()) return true;
    return false;
  }

  @Override
  public void reset() {
    myEntriesEditor.reset();
  }

  @Override
  public void saveData() {
  }

  @Override
  public void moduleStateChanged() {
    // TODO
  }

  @Override
  public void apply() throws ConfigurationException {
    myEntriesEditor.apply();
  }

  @Override
  public void disposeUIResources() {
    myEntriesEditor.disposeUIResources();
  }
}
