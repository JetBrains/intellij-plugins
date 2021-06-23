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
import com.thoughtworks.gauge.util.GaugeUtil;

import static com.thoughtworks.gauge.GaugeModuleListener.isGaugeModule;
import static com.thoughtworks.gauge.GaugeModuleListener.isGaugeProject;

public final class LibHelperFactory {
  public static final LibHelper NO_GAUGE_MODULE = new LibHelper() {
    @Override
    public void initConnection() {
    }
  };

  // Check if it is a maven module first, java deps will be added via maven so project libs dont need to be changed
  public LibHelper helperFor(Module module) {
    if (module.isDisposed()) return NO_GAUGE_MODULE;
    if (!isGaugeProject(module)) return NO_GAUGE_MODULE;

    if (GaugeUtil.isMavenModule(module) || GaugeUtil.isGradleModule(module)) {
      return new GaugeModuleLibHelper(module);
    }
    else if (isGaugeModule(module)) {
      return new GaugeLibHelper(module);
    }
    return NO_GAUGE_MODULE;
  }

  private static class GaugeModuleLibHelper extends AbstractLibHelper {
    private GaugeModuleLibHelper(Module module) {
      super(module);
    }
  }
}
