package com.intellij.tapestry.intellij;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import org.jetbrains.annotations.NotNull;

class TapestryModuleRootListener implements ModuleRootListener {
  @Override
  public void rootsChanged(@NotNull ModuleRootEvent event) {
    for (Module module : ModuleManager.getInstance((Project)event.getSource()).getModules()) {
      if (!TapestryUtils.isTapestryModule(module)) {
        return;
      }

      TapestryProject tapestryProject = TapestryModuleSupportLoader.getTapestryProject(module);
      tapestryProject.getEventsManager().modelChanged();
    }
  }
}
