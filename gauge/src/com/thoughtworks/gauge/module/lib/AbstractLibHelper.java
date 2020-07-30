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

package com.thoughtworks.gauge.module.lib;

import com.intellij.openapi.module.Module;
import com.thoughtworks.gauge.GaugeModuleListener;
import com.thoughtworks.gauge.core.Gauge;
import com.thoughtworks.gauge.util.GaugeUtil;

import static com.thoughtworks.gauge.GaugeModuleListener.isGaugeProject;

public abstract class AbstractLibHelper implements LibHelper {
  private final Module module;

  public AbstractLibHelper(Module module) {
    this.module = module;
    if (isGaugeProject(module)) {
      if (!GaugeUtil.isMavenModule(module) && !GaugeUtil.isGradleModule(module)) {
        GaugeModuleListener.makeGaugeModuleType(module);
      }
      if (Gauge.getGaugeService(module, true) == null) {
        GaugeModuleListener.createGaugeService(module);
      }
    }
  }

  public Module getModule() {
    return module;
  }
}
