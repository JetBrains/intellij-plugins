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

package com.thoughtworks.gauge.execution;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.thoughtworks.gauge.GaugeConstants;
import com.thoughtworks.gauge.exception.GaugeNotFoundException;
import com.thoughtworks.gauge.settings.GaugeSettingsModel;
import com.thoughtworks.gauge.util.GaugeUtil;

import java.util.Map;

public final class GaugeCommandLine {

  private GaugeCommandLine() {
  }

  public static GeneralCommandLine getInstance(Module module, Project project) {
    GeneralCommandLine commandLine = new GeneralCommandLine();
    try {
      GaugeSettingsModel settings = GaugeUtil.getGaugeSettings();
      commandLine.setExePath(settings.getGaugePath());
      Map<String, String> environment = commandLine.getEnvironment();
      environment.put(GaugeConstants.GAUGE_HOME, settings.getHomePath());
    }
    catch (GaugeNotFoundException e) {
      commandLine.setExePath(GaugeConstants.GAUGE);
    }
    finally {
      commandLine.setWorkDirectory(project.getBasePath());
      if (module != null) {
        commandLine.setWorkDirectory(GaugeUtil.moduleDir(module));
      }
    }
    return commandLine;
  }
}
