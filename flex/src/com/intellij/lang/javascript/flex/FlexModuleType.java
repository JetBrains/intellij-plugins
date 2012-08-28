package com.intellij.lang.javascript.flex;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;

/**
 * @author Maxim.Mossienko
 */
public class FlexModuleType extends ModuleType<FlexModuleBuilder> {
  @NonNls public static final String MODULE_TYPE_ID = "Flex";
  private static final Icon FLASH_MODULE_BIG_ICON = IconLoader.getIcon("/images/flex/flash_module_24.png");
  private static final Icon FLASH_MODULE_OPEN_ICON = IconLoader.getIcon("/images/flex/flash_module_open.png");
  private static final Icon FLASH_MODULE_CLOSED_ICON = IconLoader.getIcon("/images/flex/flash_module_closed.png");

  public FlexModuleType() {
    super(MODULE_TYPE_ID);
  }

  public FlexModuleBuilder createModuleBuilder() {
    return new FlexModuleBuilder();
  }

  public ModuleWizardStep[] createWizardSteps(final WizardContext wizardContext,
                                              final FlexModuleBuilder moduleBuilder,
                                              final ModulesProvider modulesProvider) {
    return new ModuleWizardStep[]{new SetupFlexModuleWizardStep(moduleBuilder)};
  }

  public String getName() {
    return FlexBundle.message("flash.module.type.name");
  }

  public String getDescription() {
    return FlexBundle.message("flash.module.type.description");
  }

  public Icon getBigIcon() {
    return FLASH_MODULE_BIG_ICON;
  }

  public Icon getNodeIcon(final boolean isOpened) {
    return isOpened ? FLASH_MODULE_OPEN_ICON : FLASH_MODULE_CLOSED_ICON;
  }

  public static FlexModuleType getInstance() {
    return (FlexModuleType)ModuleTypeManager.getInstance().findByID(MODULE_TYPE_ID);
  }
}
