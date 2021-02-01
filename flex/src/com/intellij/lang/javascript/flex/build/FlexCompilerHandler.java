// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.util.Function;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class FlexCompilerHandler implements ProjectComponent {
  @NotNull private final Project myProject;
  private ActiveBuildConfigurationWidget myWidget;

  public FlexCompilerHandler(@NotNull Project project) {
    myProject = project;
    MessageBusConnection connection = project.getMessageBus().connect();

    connection.subscribe(ProjectTopics.MODULES, new ModuleListener() {
      @Override
      public void modulesRenamed(@NotNull Project project, @NotNull List<? extends Module> modules, @NotNull Function<? super Module, String> oldNameProvider) {
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

  public static FlexCompilerHandler getInstance(Project project) {
    return project.getComponent(FlexCompilerHandler.class);
  }

  @Override
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

  @Override
  public void projectClosed() {
    FlexCommonUtils.deleteTempFlexConfigFiles(myProject.getName());
    FlexCompilationUtils.deleteUnzippedANEFiles();
    if (myWidget != null) {
      myWidget.destroy();
    }
  }
}
