package com.intellij.lang.javascript.flex;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.ProjectWizardStepFactory;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.PlatformUtils;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;

/**
 * @author Maxim.Mossienko
 */
public class FlexModuleType extends ModuleType<FlexModuleBuilder> {
  public static final Icon ourFlexModuleType = IconLoader.getIcon("flex_24.png", FlexModuleType.class);
  @NonNls private static final String MODULE_TYPE_ID = "Flex";
  @NonNls static final String CREATE_MODULE_HELP_ID = "reference.dialogs.new.project.fromScratch.flex";
  private static final Icon NODE_ICON_OPEN = IconLoader.getIcon("/nodes/ModuleOpen.png");
  private static final Icon NODE_ICON_CLOSED = IconLoader.getIcon("/nodes/ModuleClosed.png");

  public FlexModuleType() {
    super(MODULE_TYPE_ID);
  }

  public FlexModuleBuilder createModuleBuilder() {
    return new FlexModuleBuilder();
  }

  public ModuleWizardStep[] createWizardSteps(final WizardContext wizardContext,
                                              final FlexModuleBuilder moduleBuilder,
                                              final ModulesProvider modulesProvider) {
    final ModuleWizardStep chooseSourceFolder = ProjectWizardStepFactory.getInstance()
      .createSourcePathsStep(wizardContext, moduleBuilder, null, "reference.dialogs.new.project.fromScratch.source");
    final ModuleWizardStep createBuildAndSimpleCodeStep = new SetupFlexModuleWizardStep(moduleBuilder, wizardContext);
    return new ModuleWizardStep[]{chooseSourceFolder, createBuildAndSimpleCodeStep};
  }

  public String getName() {
    return JSBundle.message("module.type.flex.name");
  }

  public String getDescription() {
    return JSBundle.message("module.type.flex.description");
  }

  public Icon getBigIcon() {
    return ourFlexModuleType;
  }

  public Icon getNodeIcon(final boolean isOpened) {
    return !PlatformUtils.isFlexIde() ? FlexFacetType.ourFlexIcon : isOpened ? NODE_ICON_OPEN : NODE_ICON_CLOSED;
  }

  public static FlexModuleType getInstance() {
    return (FlexModuleType) ModuleTypeManager.getInstance().findByID(MODULE_TYPE_ID);
  }

}