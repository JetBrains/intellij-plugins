package com.intellij.lang.javascript.flex.actions;

import com.intellij.facet.FacetManager;
import com.intellij.lang.javascript.flex.FlexFacet;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;

public class FlexActionGroup extends DefaultActionGroup {
  @Override
  public void update(AnActionEvent e) {
    super.update(e);
    
    boolean enabled = false;
    Module module = DataKeys.MODULE.getData(e.getDataContext());
    if (module != null) {
      enabled = hasFlex(module);
    } else {
      Project project = DataKeys.PROJECT.getData(e.getDataContext());
      if (project != null) {
        for(Module m: ModuleManager.getInstance(project).getModules()) {
          if (hasFlex(m)) {
            enabled = true;
            break;
          }
        }
      }
    }
    e.getPresentation().setVisible(enabled);
  }

  private static boolean hasFlex(Module module) {
    FlexFacet flexFacet = FacetManager.getInstance(module).getFacetByType(FlexFacet.ID);
    if (flexFacet != null) return true;
    else if (ModuleType.get(module) == FlexModuleType.getInstance()) return true;
    return false;
  }
}
