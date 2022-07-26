/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge;

import com.intellij.openapi.externalSystem.model.ExternalSystemDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.thoughtworks.gauge.module.GaugeModuleType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.thoughtworks.gauge.util.GaugeUtil.isGaugeProjectDir;
import static com.thoughtworks.gauge.util.GaugeUtil.moduleDir;

public final class GaugeModuleListener implements ModuleListener {
  @Override
  public void modulesAdded(@NotNull Project project, @NotNull List<Module> modules) {
    for (Module module : modules) {
      if (module.getUserData(ExternalSystemDataKeys.NEWLY_CREATED_PROJECT) != null) {
        continue;
      }

      GaugeBootstrapService.getInstance(project).moduleAdded(module);
    }
  }

  @Override
  public void moduleRemoved(@NotNull Project project, @NotNull Module module) {
    GaugeBootstrapService.getInstance(project).moduleRemoved(module);
  }

  /**
   * Sets the type of the given module to that of a Gauge module
   *
   * @param module the module to be set
   */
  public static void makeGaugeModuleType(Module module) {
    module.setModuleType(GaugeModuleType.MODULE_TYPE_ID);
  }

  /**
   * Returns whether the module is a Gauge module. A module is a Gauge module if either its module type name
   * indicates that it is a Gauge module, or if it is a Gauge project.
   *
   * @param module the module to be examined
   * @return whether the module is a Gauge module.
   */
  public static boolean isGaugeModule(Module module) {
    return GaugeModuleType.MODULE_TYPE_ID.equals(module.getModuleTypeName()) || isGaugeProjectDir(moduleDir(module));
  }

  /**
   * Returns whether or not the module is a Gauge project. A module is a Gauge project if the module is also a
   * Gauge project directory (i.e. it has a `specs` directory and other required components).
   *
   * @param module the module to be examined
   * @return whether or not the module is a Gauge project
   */
  public static boolean isGaugeProject(Module module) {
    return isGaugeProjectDir(moduleDir(module));
  }
}
