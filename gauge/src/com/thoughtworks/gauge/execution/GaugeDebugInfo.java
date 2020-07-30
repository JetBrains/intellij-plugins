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
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.thoughtworks.gauge.util.SocketUtils;

public final class GaugeDebugInfo {
  private final boolean shouldDebug;
  private final String port;
  private static final String host = "localhost";

  private GaugeDebugInfo(boolean shouldDebug, String port) {
    this.shouldDebug = shouldDebug;
    this.port = port;
  }

  public boolean shouldDebug() {
    return shouldDebug;
  }

  public String getPort() {
    return port;
  }

  public int getPortInt() {
    return Integer.parseInt(port);
  }

  public String getHost() {
    return host;
  }

  public static GaugeDebugInfo getInstance(GeneralCommandLine commandLine, ExecutionEnvironment env) {
    if (isDebugExecution(env)) {
      String port = debugPort();
      commandLine.getEnvironment().put("GAUGE_DEBUG_OPTS", port);
      return new GaugeDebugInfo(true, port);
    }
    return new GaugeDebugInfo(false, "");
  }

  public static boolean isDebugExecution(ExecutionEnvironment env) {
    return DefaultDebugExecutor.EXECUTOR_ID.equals(env.getExecutor().getId());
  }

  private static String debugPort() {
    return String.valueOf(SocketUtils.findFreePortForApi());
  }
}