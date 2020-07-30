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

package com.thoughtworks.gauge.inspection;

import com.intellij.analysis.AnalysisScope;
import com.intellij.codeInspection.GlobalInspectionContext;
import com.intellij.codeInspection.GlobalInspectionTool;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptionsProcessor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.thoughtworks.gauge.util.GaugeUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public final class GlobalInspectionProvider extends GlobalInspectionTool {
  @Override
  public void runInspection(@NotNull AnalysisScope scope, @NotNull InspectionManager manager,
                            @NotNull GlobalInspectionContext globalContext, @NotNull ProblemDescriptionsProcessor processor) {
    GaugeErrors.init();
    Module[] modules = ModuleManager.getInstance(globalContext.getProject()).getModules();
    for (Module module : modules) {
      File dir = GaugeUtil.moduleDir(module);
      GaugeErrors.add(dir.getAbsolutePath(), GaugeInspectionHelper.getErrors(dir));
    }
  }
}
