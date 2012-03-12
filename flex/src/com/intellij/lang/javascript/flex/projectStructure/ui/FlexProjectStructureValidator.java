package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.library.FlexLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.model.DependencyEntry;
import com.intellij.lang.javascript.flex.projectStructure.model.SharedLibraryEntry;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.ui.configuration.projectRoot.daemon.*;
import com.intellij.openapi.util.Condition;
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

  @Override
  protected boolean addLibraryToDependencies(final Library library, final Project project, final boolean allowEmptySelection) {
    if (!(((LibraryEx)library).getType() instanceof FlexLibraryType)) {
      return false;
    }

    ChooseBuildConfigurationDialog d = ChooseBuildConfigurationDialog
      .createForApplicableBCs(FlexBundle.message("choose.bc.dialog.title"), FlexBundle.message("choose.bc.dialog.label", library.getName()),
                              project, allowEmptySelection, new Condition<FlexIdeBCConfigurable>() {
        @Override
        public boolean value(final FlexIdeBCConfigurable configurable) {
          for (DependencyEntry entry : configurable.getEditableObject().getDependencies().getEntries()) {
            if (entry instanceof SharedLibraryEntry) {
              if (((SharedLibraryEntry)entry).getLibraryName().equals(library.getName()) &&
                  ((SharedLibraryEntry)entry).getLibraryLevel().equals(library.getTable().getTableLevel())) {
                return false;
              }
            }
          }
          return true;
        }
      });
    if (d == null) {
      return true;
    }

    d.show();
    if (!d.isOK()) {
      return true;
    }

    for (FlexIdeBCConfigurable c : d.getSelectedConfigurables()) {
      c.addSharedLibrary(library);
    }
    return true;
  }
}
