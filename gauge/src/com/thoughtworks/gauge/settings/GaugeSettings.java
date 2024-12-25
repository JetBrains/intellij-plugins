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

package com.thoughtworks.gauge.settings;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.util.Disposer;
import com.thoughtworks.gauge.GaugeBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

final class GaugeSettings implements SearchableConfigurable, Disposable {
  private GaugeConfig gaugeConfig;
  private GaugeSettingsModel model;

  @Override
  public @NotNull String getId() {
    return "gauge";
  }

  @Override
  public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
    return GaugeBundle.GAUGE;
  }

  @Override
  public @Nullable JComponent createComponent() {
    model = GaugeSettingsService.getSettings();
    gaugeConfig = new GaugeConfig();
    gaugeConfig.setValues(model);
    return gaugeConfig.createEditor();
  }

  @Override
  public boolean isModified() {
    return !model.equals(gaugeConfig.getValues());
  }

  @Override
  public void apply() {
    model = gaugeConfig.getValues();
    GaugeSettingsService.getService().loadState(model);
  }

  @Override
  public void reset() {
    gaugeConfig.setValues(model);
  }

  @Override
  public void disposeUIResources() {
    Disposer.dispose(this);
  }

  @Override
  public void dispose() {
    gaugeConfig = null;
  }
}
