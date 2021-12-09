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

import com.intellij.openapi.util.NlsSafe;
import com.thoughtworks.gauge.GaugeConstants;

import java.util.Objects;

public final class GaugeSettingsModel {
  public String gaugePath;
  public String homePath;
  public Boolean useIntelliJTestRunner;

  public GaugeSettingsModel(String gaugePath, String homePath, Boolean useIntelliJTestRunner) {
    this.gaugePath = gaugePath;
    this.homePath = homePath;
    this.useIntelliJTestRunner = useIntelliJTestRunner;
  }

  public GaugeSettingsModel() {
    this("", "", true);
  }

  public @NlsSafe String getGaugePath() {
    return gaugePath;
  }

  public String getHomePath() {
    return homePath == null ? System.getenv(GaugeConstants.GAUGE_HOME) : homePath;
  }

  public Boolean useIntelliJTestRunner() {
    return useIntelliJTestRunner;
  }

  public boolean isGaugePathSet() {
    return gaugePath != null && !gaugePath.isEmpty();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    GaugeSettingsModel that = (GaugeSettingsModel)o;
    return (Objects.equals(gaugePath, that.gaugePath))
           && (Objects.equals(homePath, that.homePath)) &&
           useIntelliJTestRunner() == that.useIntelliJTestRunner();
  }

  @Override
  public String toString() {
    return "GaugeSettingsModel{" +
           "gaugePath='" + gaugePath + '\'' +
           ", homePath='" + homePath + '\'' +
           ", useIntelliJTestRunner=" + useIntelliJTestRunner +
           '}';
  }
}
