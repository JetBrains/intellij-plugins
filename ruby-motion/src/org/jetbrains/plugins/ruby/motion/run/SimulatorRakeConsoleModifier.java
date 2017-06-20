/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.ruby.motion.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.jetbrains.cidr.execution.debugger.CidrDebuggerSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.console.config.IrbConsoleBuilder;
import org.jetbrains.plugins.ruby.console.config.IrbRunConfiguration;
import org.jetbrains.plugins.ruby.console.config.IrbRunConfigurationType;
import org.jetbrains.plugins.ruby.motion.RubyMotionUtil;
import org.jetbrains.plugins.ruby.ruby.run.configuration.RubyProgramRunner;
import org.jetbrains.plugins.ruby.tasks.rake.runConfigurations.RakeConsoleModifier;
import org.jetbrains.plugins.ruby.tasks.rake.runConfigurations.RakeRunConfiguration;

/**
 * @author Dennis.Ushakov
 */
public class SimulatorRakeConsoleModifier implements RakeConsoleModifier {
  @Override
  public boolean isApplicable(@NotNull RakeRunConfiguration config) {
    final Module module = config.getModule();
    return RubyMotionUtil.getInstance().hasRubyMotionSupport(module);
  }

  @NotNull
  @Override
  public TextConsoleBuilder createConsoleBuilder(@NotNull RakeRunConfiguration config) {
    if (onDevice(config)) {
      return TextConsoleBuilderFactory.getInstance().createBuilder(config.getProject());
    }
    final ConfigurationFactory factory = IrbRunConfigurationType.getInstance().getConfigurationFactories()[0];
    final Project project = config.getProject();
    return new IrbConsoleBuilder(project, (IrbRunConfiguration)factory.createTemplateConfiguration(project));
  }

  @NotNull
  @Override
  public ProcessHandler createProcessHandler(RakeRunConfiguration config, final GeneralCommandLine cmd, String runnerId) throws ExecutionException {
    final MotionSimulatorRunExtension.MotionProcessOutputReaders readers = cmd.getUserData(MotionSimulatorRunExtension.FILE_OUTPUT_READERS);
    cmd.putUserData(MotionSimulatorRunExtension.FILE_OUTPUT_READERS, null);
    CidrDebuggerSettings.getInstance().VALUES_FILTER_ENABLED = false;
    assert readers != null;
    final Module module = config.getModule();
    assert module != null;
    return onDevice(config) ?
           new MotionDeviceProcessHandler(cmd.createProcess(), cmd, runnerId) :
           simulator(config) ?
           new SimulatorConsoleProcessHandler(module, cmd.createProcess(), cmd.getCommandLineString(), readers) :
           new MotionProcessHandler(module, cmd.createProcess(), cmd.getCommandLineString(), runnerId, readers);
  }

  @NotNull
  @Override
  public String overrideRunnerId(String id) {
    return RubyProgramRunner.RUBY_RUNNER_ID;
  }

  private static boolean simulator(RakeRunConfiguration config) {
    final Module module = config.getModule();
    assert module != null;
    return RubyMotionUtil.getInstance().getMainRakeTask(module).equals(config.getTaskName());
  }

  private static boolean onDevice(RakeRunConfiguration config) {
    return config.getTaskName().contains("device");
  }
}
