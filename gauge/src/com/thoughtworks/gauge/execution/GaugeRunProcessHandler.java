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

import com.intellij.debugger.impl.GenericDebuggerRunner;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl;
import com.intellij.execution.process.ColoredProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.remote.RemoteConfiguration;
import com.intellij.execution.remote.RemoteConfigurationType;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

public final class GaugeRunProcessHandler extends ColoredProcessHandler {
  private static final Logger LOG = Logger.getInstance(GaugeRunProcessHandler.class);

  private GaugeRunProcessHandler(Process process, String commandLineString) {
    super(process, commandLineString);
  }

  public static GaugeRunProcessHandler runCommandLine(final GeneralCommandLine commandLine, GaugeDebugInfo debugInfo, Project project)
    throws ExecutionException {
    LOG.info(String.format("Running Gauge tests with command : %s", commandLine.getCommandLineString()));
    final GaugeRunProcessHandler gaugeRunProcess =
      new GaugeRunProcessHandler(commandLine.createProcess(), commandLine.getCommandLineString());
    ProcessTerminatedListener.attach(gaugeRunProcess);
    if (debugInfo.shouldDebug()) {
      launchDebugger(project, debugInfo);
    }
    return gaugeRunProcess;
  }

  private static void launchDebugger(final Project project, final GaugeDebugInfo debugInfo) {
    Runnable runnable = () -> {
      final long startTime = System.currentTimeMillis();
      GenericDebuggerRunner basicProgramRunner = new GenericDebuggerRunner();
      RunManagerImpl manager = new RunManagerImpl(project);
      ConfigurationFactory configFactory = RemoteConfigurationType.getInstance().getConfigurationFactories()[0];
      RemoteConfiguration remoteConfig = new RemoteConfiguration(project, configFactory);
      remoteConfig.PORT = debugInfo.getPort();
      remoteConfig.HOST = debugInfo.getHost();
      remoteConfig.USE_SOCKET_TRANSPORT = true;
      remoteConfig.SERVER_MODE = false;
      RunnerAndConfigurationSettingsImpl configuration = new RunnerAndConfigurationSettingsImpl(manager, remoteConfig, false);
      ExecutionEnvironment environment = new ExecutionEnvironment(new DefaultDebugExecutor(), basicProgramRunner, configuration, project);

      boolean debuggerConnected = false;
      // Trying to connect to gauge java for 25 secs. The sleep is because it may take a few seconds for gauge to launch the java process and the jvm to load after that
      while (!debuggerConnected && ((System.currentTimeMillis() - startTime) < 25000)) {
        try {
          //noinspection BusyWait
          Thread.sleep(5000);
          basicProgramRunner.execute(environment);
          debuggerConnected = true;
        }
        catch (Exception e) {
          LOG.warn("Failed to connect debugger. Retrying... : " + e.getMessage());
          LOG.debug(e);
        }
      }
    };

    ApplicationManager.getApplication().invokeAndWait(runnable, ModalityState.any());
  }
}
