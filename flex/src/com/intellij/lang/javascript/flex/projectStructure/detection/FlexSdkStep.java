package com.intellij.lang.javascript.flex.projectStructure.detection;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.sdk.FlexSdkComboBoxWithBrowseButton;
import com.intellij.lang.javascript.flex.sdk.FlexmojosSdkType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class FlexSdkStep extends ModuleWizardStep {
  private JPanel myContentPane;
  private FlexSdkComboBoxWithBrowseButton mySdkCombo;
  private WizardContext myContext;

  public FlexSdkStep(WizardContext context) {
    myContext = context;
    selectSdkUsedByOtherModules(myContext.getProject(), mySdkCombo);
  }

  public JComponent getComponent() {
    return myContentPane;
  }

  public void updateDataModel() {
    myContext.setProjectJdk(mySdkCombo.getSelectedSdk());
  }

  // todo fix that all
  public static void selectSdkUsedByOtherModules(final @Nullable Project project,
                                                 final FlexSdkComboBoxWithBrowseButton flexSdkComboWithBrowse) {
    if (project != null) {
      for (final Module module : ModuleManager.getInstance(project).getModules()) {
        if (FlexUtils.isFlexModuleOrContainsFlexFacet(module)) {
          final Sdk sdk = FlexUtils.getFlexSdkForFlexModuleOrItsFlexFacets(module);
          if (sdk != null && !(sdk.getSdkType() instanceof FlexmojosSdkType)) {
            flexSdkComboWithBrowse.setSelectedSdkRaw(sdk.getName());
            return;
          }
        }
      }
    }
  }
}
