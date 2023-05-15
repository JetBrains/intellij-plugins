package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleConfigurationEditor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ui.configuration.CommonContentEntriesEditor;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nls;
import org.jetbrains.jps.model.java.JavaSourceRootType;

import javax.swing.*;
import java.util.List;

public class FlexModuleEditor implements ModuleConfigurationEditor {
  public static final String DISPLAY_NAME = "Flex";

  private final CommonContentEntriesEditor myEntriesEditor;

  public FlexModuleEditor(ModuleConfigurationState state) {
    Module module = state.getCurrentRootModel().getModule();
    myEntriesEditor = new CommonContentEntriesEditor(module.getName(), state, JavaSourceRootType.SOURCE, JavaSourceRootType.TEST_SOURCE) {
      @Override
      protected List<ContentEntry> addContentEntries(VirtualFile[] files) {
        List<ContentEntry> entries = super.addContentEntries(files);
        addContentEntryPanels(entries.toArray(new ContentEntry[0]));
        return entries;
      }
    };
    myEntriesEditor.getComponent().setBorder(JBUI.Borders.empty());
  }

  @Nls
  @Override
  public String getDisplayName() {
    return DISPLAY_NAME;
  }

  @Override
  public String getHelpTopic() {
    return "projectStructure.modules.flex";
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
  public void apply() throws ConfigurationException {
    myEntriesEditor.apply();
  }

  @Override
  public void disposeUIResources() {
    myEntriesEditor.disposeUIResources();
  }
}
