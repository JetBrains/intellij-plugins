// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

internal class TfRunExistingConfigAction(private val settings: RunnerAndConfigurationSettings) : AnAction() {
  init {
    templatePresentation.text = settings.name
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return

    val runManager: RunManager = RunManager.getInstance(project)
    runManager.selectedConfiguration = settings
    ProgramRunnerUtil.executeConfiguration(settings, DefaultRunExecutor.getRunExecutorInstance())
  }
}