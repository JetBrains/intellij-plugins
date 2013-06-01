package com.jetbrains.lang.dart.ide.module;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.ProjectJdkForModuleStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.DartSdkType;

import javax.swing.*;

/**
 * @author: Fedor.Korotkov
 */
public class DartModuleType extends DartModuleTypeBase<DartModuleBuilder> {
  private static final String MODULE_TYPE_ID = "DART_MODULE";

  public DartModuleType() {
    super(MODULE_TYPE_ID);
  }

  public static DartModuleType getInstance() {
    return (DartModuleType)ModuleTypeManager.getInstance().findByID(MODULE_TYPE_ID);
  }

  @Override
  public String getName() {
    return DartBundle.message("dart.module.type.name");
  }

  @Override
  public String getDescription() {
    return DartBundle.message("dart.module.type.description");
  }

  @Override
  public Icon getBigIcon() {
    return icons.DartIcons.Dart_24;
  }

  @Override
  public Icon getNodeIcon(boolean isOpened) {
    return icons.DartIcons.Dart_16;
  }

  @Override
  public DartModuleBuilder createModuleBuilder() {
    return new DartModuleBuilder();
  }


  public ModuleWizardStep[] createWizardSteps(final WizardContext wizardContext,
                                              final DartModuleBuilder moduleBuilder,
                                              final ModulesProvider modulesProvider) {
    return new ModuleWizardStep[]{new ProjectJdkForModuleStep(wizardContext, DartSdkType.getInstance()) {
      public void updateDataModel() {
        super.updateDataModel();
        moduleBuilder.setModuleJdk(getJdk());
      }
    }};
  }
}
