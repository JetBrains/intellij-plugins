package com.intellij.lang.javascript.flex.projectStructure.detection;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.FlexBuildConfigurationsExtension;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableFlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.SdkEntry;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.lang.javascript.flex.sdk.FlexSdkComboBoxWithBrowseButton;
import com.intellij.lang.javascript.flex.sdk.FlexSdkType2;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class FlexSdkStep extends ModuleWizardStep {
  private JPanel myContentPane;
  private FlexSdkComboBoxWithBrowseButton mySdkCombo;
  private JLabel mySdkLabel;
  private final WizardContext myContext;

  public FlexSdkStep(WizardContext context) {
    myContext = context;
    selectSdkUsedByOtherModules(myContext.getProject(), mySdkCombo);
  }

  @Override
  public JComponent getComponent() {
    mySdkLabel.setLabelFor(mySdkCombo.getChildComponent());
    final String text = FlexBundle.message(myContext.getProject() != null ? "module.sdk.label" : "project.sdk.label");
    mySdkLabel.setText(UIUtil.removeMnemonic(text));
    mySdkLabel.setDisplayedMnemonicIndex(UIUtil.getDisplayMnemonicIndex(text));

    mySdkCombo.setButtonEnabled(FlexBuildConfigurationsExtension.getInstance().getConfigurator().getConfigEditor() == null);
    return myContentPane;
  }

  @Override
  public void updateDataModel() {
    myContext.setProjectJdk(mySdkCombo.getSelectedSdk());
  }

  private static void selectSdkUsedByOtherModules(final @Nullable Project project, final FlexSdkComboBoxWithBrowseButton combo) {
    if (project == null) {
      return;
    }

    final FlexProjectConfigurationEditor currentEditor = FlexBuildConfigurationsExtension.getInstance().getConfigurator().getConfigEditor();
    if (currentEditor != null) {
      final Module[] modules = ProjectStructureConfigurable.getInstance(project).getModulesConfig().getModules();
      for (Module module : modules) {
        if (ModuleType.get(module) != FlexModuleType.getInstance()) {
          continue;
        }
        for (ModifiableFlexBuildConfiguration c : currentEditor.getConfigurations(module)) {
          SdkEntry sdkEntry = c.getDependencies().getSdkEntry();
          Sdk sdk;
          if (sdkEntry != null &&
              (sdk = FlexSdkUtils.findFlexOrFlexmojosSdk(sdkEntry.getName())) != null &&
              sdk.getSdkType() == FlexSdkType2.getInstance()) {
            combo.setSelectedSdkRaw(sdk.getName());
            return;
          }
        }
      }
    }
    else {
      for (final Module module : ModuleManager.getInstance(project).getModules()) {
        if (ModuleType.get(module) == FlexModuleType.getInstance()) {
          for (FlexBuildConfiguration bc : FlexBuildConfigurationManager.getInstance(module).getBuildConfigurations()) {
            Sdk sdk = bc.getSdk();
            if (sdk != null) {
              combo.setSelectedSdkRaw(sdk.getName());
              return;
            }
          }
        }
      }
    }
  }
}
