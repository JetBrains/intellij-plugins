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

import com.thoughtworks.gauge.connection.GaugeConnection;

public final class GaugeCli {
  private final Process gaugeProcess;
  private final Thread gaugeExceptionWatcher;
  private final GaugeConnection gaugeConnection;

  public GaugeCli(Process gaugeProcess, Thread watcher, GaugeConnection gaugeConnection) {
    this.gaugeProcess = gaugeProcess;
    gaugeExceptionWatcher = watcher;
    this.gaugeConnection = gaugeConnection;
  }

  public Thread getExceptionWatcher() {
    return gaugeExceptionWatcher;
  }

  public GaugeConnection getGaugeConnection() {
    return gaugeConnection;
  }

  public Process getGaugeProcess() {
    return gaugeProcess;
  }
}
