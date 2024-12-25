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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.openapi.components.RoamingType.DISABLED;

@State(
  name = "GaugeConfiguration",
  storages = {
    @Storage(value = "GaugeConfig.xml", roamingType = DISABLED),
  }
)
public final class GaugeSettingsService implements PersistentStateComponent<GaugeSettingsModel> {
  private GaugeSettingsModel state = new GaugeSettingsModel();

  @Override
  public @Nullable GaugeSettingsModel getState() {
    return state;
  }

  @Override
  public void loadState(@NotNull GaugeSettingsModel state) {
    this.state = state;
  }

  public static GaugeSettingsModel getSettings() {
    GaugeSettingsService service = getService();
    return service.getState();
  }

  public static GaugeSettingsService getService() {
    return ApplicationManager.getApplication().getService(GaugeSettingsService.class);
  }
}
