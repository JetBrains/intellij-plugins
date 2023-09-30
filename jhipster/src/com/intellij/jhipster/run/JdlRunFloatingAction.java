// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.run;

import com.intellij.execution.ExecutorRegistryImpl.RunnerHelper;
import com.intellij.execution.RunManager;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.jhipster.JdlFileType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.util.io.FileUtil.toSystemDependentName;
import static com.intellij.openapi.vfs.VfsUtilCore.virtualToIoFile;

final class JdlRunFloatingAction extends AnAction implements DumbAware {
  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    var psiFile = e.getData(CommonDataKeys.PSI_FILE);
    e.getPresentation().setEnabledAndVisible(psiFile != null && psiFile.getFileType() == JdlFileType.INSTANCE);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    var project = e.getProject();
    if (project == null) return;

    var psiFile = e.getData(CommonDataKeys.PSI_FILE);
    if (psiFile == null) return;

    var jdlFile = psiFile.getVirtualFile();
    if (jdlFile == null) return;

    var runManager = RunManager.getInstance(project);

    var runConfiguration = findRunConfiguration(project, jdlFile);
    if (runConfiguration == null) {
      var uniqueName = runManager.suggestUniqueName(jdlFile.getName(), JdlRunConfigurationType.getInstance());
      var runnerAndConfigurationSettings = runManager.createConfiguration(uniqueName, JdlRunConfigurationType.class);
      var newConfiguration = (JdlRunConfiguration)runnerAndConfigurationSettings.getConfiguration();
      newConfiguration.getOptions().setJdlLocation(toSystemDependentName(virtualToIoFile(jdlFile).getPath()));
      runManager.addConfiguration(runnerAndConfigurationSettings);

      runConfiguration = newConfiguration;
    }

    var settings = runManager.findSettings(runConfiguration);

    RunnerHelper.run(project, runConfiguration, settings, e.getDataContext(), DefaultRunExecutor.getRunExecutorInstance());
  }

  private static RunConfiguration findRunConfiguration(Project project, VirtualFile jdlFile) {
    var runConfigurations = RunManager.getInstance(project)
      .getConfigurationsList(JdlRunConfigurationType.getInstance());

    for (var runConfiguration : runConfigurations) {
      if (runConfiguration instanceof JdlRunConfiguration) {
        var jdlLocation = ((JdlRunConfiguration)runConfiguration).getOptions().getJdlLocation();
        //noinspection UnstableApiUsage
        if (VfsUtilCore.pathEqualsTo(jdlFile, jdlLocation)) {
          return runConfiguration;
        }
      }
    }
    return null;
  }
}
