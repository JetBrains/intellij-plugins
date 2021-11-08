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

package com.thoughtworks.gauge.properties;

import com.intellij.lang.properties.codeInspection.unused.ImplicitPropertyUsageProvider;
import com.intellij.lang.properties.psi.Property;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.PsiFile;
import com.thoughtworks.gauge.util.GaugeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

final class GaugeImplicitPropertyUsageProvider implements ImplicitPropertyUsageProvider {
  private static final Set<String> GAUGE_DEFAULT_PROPERTIES = Set.of(
    "gauge_reports_dir",
    "overwrite_reports",
    "screenshot_on_failure",
    "logs_directory",
    "enable_multithreading",
    "gauge_specs_dir",
    "csv_delimiter",
    "allow_multiline_step"
  );

  private static final Set<String> GAUGE_JAVA_PROPERTIES = Set.of(
    "gauge_java_home",
    "gauge_custom_build_path",
    "gauge_additional_libs",
    "gauge_jvm_args",
    "gauge_custom_compile_dir",
    "gauge_clear_state_level"
  );

  @Override
  public boolean isUsed(@NotNull Property property) {
    String propertyName = property.getName();

    if (!GAUGE_DEFAULT_PROPERTIES.contains(propertyName) && !GAUGE_JAVA_PROPERTIES.contains(propertyName)) {
      return false;
    }

    PsiFile containingFile = property.getContainingFile();
    if (containingFile == null) return false;

    String fileName = containingFile.getName();

    if (GAUGE_DEFAULT_PROPERTIES.contains(propertyName) && "default.properties".equals(fileName)
        || GAUGE_JAVA_PROPERTIES.contains(propertyName) && "java.properties".equals(fileName)) {
      Module module = ModuleUtilCore.findModuleForFile(containingFile);
      return module != null && GaugeUtil.isGaugeModule(module);
    }

    return false;
  }
}
