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

package com.thoughtworks.gauge.module;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import com.thoughtworks.gauge.GaugeBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class GaugeModuleType extends ModuleType<JavaModuleBuilder> {
  // legacy module type, kept for compatibility with old projects
  public static final String MODULE_TYPE_ID = "Gauge_Module";

  public GaugeModuleType() {
    super(MODULE_TYPE_ID);
  }

  public static GaugeModuleType getInstance() {
    return (GaugeModuleType)ModuleTypeManager.getInstance().findByID(MODULE_TYPE_ID);
  }

  @Override
  public @NotNull JavaModuleBuilder createModuleBuilder() {
    return new JavaModuleBuilder();
  }

  @Override
  public @NotNull String getName() {
    return GaugeBundle.message("gauge.module.type.name");
  }

  @Override
  public @NotNull String getDescription() {
    return GaugeBundle.message("module.supported.for.writing.gauge.tests");
  }

  @Override
  public @NotNull Icon getNodeIcon(@Deprecated boolean isOpened) {
    return AllIcons.Nodes.Module;
  }
}
