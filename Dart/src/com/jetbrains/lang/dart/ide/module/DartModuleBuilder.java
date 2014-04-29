package com.jetbrains.lang.dart.ide.module;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class DartModuleBuilder extends ModuleBuilder {

  @NotNull
  private final DartProjectTemplate<?> myTemplate;

  public DartModuleBuilder(@NotNull final DartProjectTemplate<?> template) { myTemplate = template; }

  @Override
  public boolean isTemplateBased() { return true; }

  @Override
  public String getGroupName() {
    return DartModuleType.GROUP_NAME;
  }

  @Override
  public ModuleType getModuleType() { return DartModuleType.getInstance(); }

  @Override
  public Icon getNodeIcon() { return myTemplate.getIcon(); }

  @Override
  public String getPresentableName() {
    return getGroupName();
  }

  @Override
  public void setupRootModel(ModifiableRootModel modifiableRootModel) throws ConfigurationException {
    doAddContentEntry(modifiableRootModel);
  }

  @Nullable
  @Override
  public Module commitModule(@NotNull Project project, @Nullable ModifiableModuleModel model) {
    Module module = super.commitModule(project, model);
    if (module != null) {
      doGenerate(myTemplate, module);
    }
    return module;
  }

  @Nullable
  @Override
  public ModuleWizardStep modifySettingsStep(@NotNull SettingsStep settingsStep) {
    final DartProjectGenerator.GeneratorPeer peer = myTemplate.getPeer();
    peer.buildUI(settingsStep);
    return new ModuleWizardStep() {
      @Override
      public JComponent getComponent() {
        return null;
      }

      @Override
      public void updateDataModel() {
      }

      @Override
      public boolean validate() throws ConfigurationException {
        ValidationInfo info = peer.validate();
        if (info != null) throw new ConfigurationException(info.message);
        return true;
      }
    };
  }

  private static <T> void doGenerate(final DartProjectTemplate<T> template, final Module module) {
      DartProjectGenerator.GeneratorPeer<T> peer = template.getPeer();
      ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
      VirtualFile[] contentRoots = moduleRootManager.getContentRoots();
      VirtualFile dir = module.getProject().getBaseDir();
      if (contentRoots.length > 0 && contentRoots[0] != null) {
        dir = contentRoots[0];
      }
      template.generateProject(module.getProject(), dir, peer.getSettings(), module);
  }

}
