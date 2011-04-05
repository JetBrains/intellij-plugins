package com.intellij.javascript.flex.css;

import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurableProvider;
import com.intellij.openapi.project.Project;

/**
 * @author Eugene.Kudelevsky
 */
public class CssDialectsConfigurableProvider extends ConfigurableProvider {
  private final Project myProject;

  public CssDialectsConfigurableProvider(Project project) {
    myProject = project;
  }

  @Override
  public Configurable createConfigurable() {
    return isVisible() ? new CssDialectsConfigurable(myProject) : null;
  }

  private boolean isVisible() {
    final Module[] modules = ModuleManager.getInstance(myProject).getModules();

    for (Module module : modules) {
      if (FlexUtils.isFlexModuleOrContainsFlexFacet(module)) {
        return true;
      }
    }
    return false;
  }
}
