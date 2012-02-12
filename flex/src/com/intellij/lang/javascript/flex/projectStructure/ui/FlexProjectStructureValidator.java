package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ui.configuration.projectRoot.daemon.*;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

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

  @Nullable
  @Override
  protected List<ProjectStructureElementUsage> getUsagesIn(final ProjectStructureElement element) {
    if (element instanceof ModuleProjectStructureElement) {
      Module module = ((ModuleProjectStructureElement)element).getModule();
      if (ModuleType.get(module) == FlexModuleType.getInstance()) {
        // all the usages will be reported for Flex build configurations
        return Collections.emptyList();
      }
    }
    return null;
  }
}
