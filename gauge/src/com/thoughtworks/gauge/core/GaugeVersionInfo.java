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

package com.thoughtworks.gauge.core;

import java.util.ArrayList;
import java.util.List;

public class GaugeVersionInfo {
  public String version;
  public List<Plugin> plugins;

  public GaugeVersionInfo(String v, List<Plugin> plugins) {
    this.version = v;
    this.plugins = plugins;
  }

  public GaugeVersionInfo(String v) {
    this.version = v;
    this.plugins = new ArrayList<>();
  }

  public GaugeVersionInfo() {
  }

  public Boolean isPluginInstalled(String plugin) {
    return plugins.stream().anyMatch(p -> plugin.equalsIgnoreCase(p.name));
  }

  Boolean isGreaterOrEqual(GaugeVersionInfo versionInfo) {
    return this.version != null && this.version.compareTo(versionInfo.version) >= 0;
  }
}
