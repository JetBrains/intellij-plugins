package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ui.configuration.projectRoot.daemon.ModuleProjectStructureElement;
import com.intellij.openapi.roots.ui.configuration.projectRoot.daemon.ProjectStructureElement;
import com.intellij.openapi.roots.ui.configuration.projectRoot.daemon.ProjectStructureProblemsHolder;
import com.intellij.openapi.roots.ui.configuration.projectRoot.daemon.ProjectStructureValidator;

/**
 * User: ksafonov
 */
public class FlexProjectStructureValidator extends ProjectStructureValidator {
  
  @Override
  protected boolean checkElement(final ProjectStructureElement element, final ProjectStructureProblemsHolder problemsHolder) {
    if (element instanceof ModuleProjectStructureElement) {
      Module module = ((ModuleProjectStructureElement)element).getModule();
      if (ModuleType.get(module) == FlexModuleType.getInstance()) {
        checkModuleElement((ModuleProjectStructureElement)element, problemsHolder);
        return true;
      }
    }
    return false;
  }

  private static void checkModuleElement(final ModuleProjectStructureElement e, final ProjectStructureProblemsHolder problemsHolder) {
    e.checkModulesNames(problemsHolder);
    // all the other stuff will be checked at BC level
  }
}
