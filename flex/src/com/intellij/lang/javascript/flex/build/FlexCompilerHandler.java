// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.intellij.lang.javascript.flex.build;

import com.intellij.ProjectTopics;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.flex.FlexCommonUtils;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitAfterCompileTask;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitPrecompileTask;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexBuildConfigurationChangeListener;
import com.intellij.lang.javascript.flex.projectStructure.ui.ActiveBuildConfigurationWidget;
import com.intellij.lang.javascript.flex.run.FlashRunConfiguration;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.util.Function;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class FlexCompilerHandler extends AbstractProjectComponent {
  private ActiveBuildConfigurationWidget myWidget;

  public FlexCompilerHandler(final Project project) {
    super(project);

    MessageBusConnection connection = project.getMessageBus().connect(project);

    connection.subscribe(ProjectTopics.MODULES, new ModuleListener() {
      @Override
      public void modulesRenamed(@NotNull Project project, @NotNull List<Module> modules, @NotNull Function<Module, String> oldNameProvider) {
        for (RunnerAndConfigurationSettings settings : RunManager.getInstance(project).getAllSettings()) {
          RunConfiguration runConfiguration = settings.getConfiguration();
          if (runConfiguration instanceof FlashRunConfiguration) {
            ((FlashRunConfiguration)runConfiguration).getRunnerParameters().handleModulesRename(modules, oldNameProvider);
          }
          else if (runConfiguration instanceof FlexUnitRunConfiguration) {
            ((FlexUnitRunConfiguration)runConfiguration).getRunnerParameters().handleModulesRename(modules, oldNameProvider);
          }
        }
      }
    });

    connection.subscribe(FlexBuildConfigurationChangeListener.TOPIC, new FlexBuildConfigurationChangeListener() {
      @Override
      public void buildConfigurationsRenamed(final Map<Pair<String, String>, String> renames) {
        for (RunnerAndConfigurationSettings settings : RunManager.getInstance(project).getAllSettings()) {
          RunConfiguration runConfiguration = settings.getConfiguration();
          if (runConfiguration instanceof FlashRunConfiguration) {
            ((FlashRunConfiguration)runConfiguration).getRunnerParameters().handleBuildConfigurationsRename(renames);
          }
          else if (runConfiguration instanceof FlexUnitRunConfiguration) {
            ((FlexUnitRunConfiguration)runConfiguration).getRunnerParameters().handleBuildConfigurationsRename(renames);
          }
        }
      }
    });
  }

  @NotNull
  public String getComponentName() {
    return "FlexCompilerHandler";
  }

  public static FlexCompilerHandler getInstance(Project project) {
    return project.getComponent(FlexCompilerHandler.class);
  }

  public void projectOpened() {
    CompilerManager compilerManager = CompilerManager.getInstance(myProject);
    if (compilerManager != null) {
      compilerManager.addBeforeTask(new ValidateFlashConfigurationsPrecompileTask());
      compilerManager.addBeforeTask(new FlexUnitPrecompileTask(myProject));
      compilerManager.addAfterTask(new FlexUnitAfterCompileTask());

      compilerManager.setValidationEnabled(FlexModuleType.getInstance(), false);
    }

    myWidget = new ActiveBuildConfigurationWidget(myProject);
  }

  public void projectClosed() {
    FlexCommonUtils.deleteTempFlexConfigFiles(myProject.getName());
    FlexCompilationUtils.deleteUnzippedANEFiles();
    myWidget.destroy();
  }
}
