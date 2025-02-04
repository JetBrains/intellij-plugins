// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.execution.RunManagerListener
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import org.intellij.terraform.isTerraformCompatibleExtension

private class TfRunManagerListener(val project: Project) : RunManagerListener {

  override fun runConfigurationAdded(settings: RunnerAndConfigurationSettings): Unit = updateIfTfCompatible(settings)
  override fun runConfigurationRemoved(settings: RunnerAndConfigurationSettings): Unit = updateIfTfCompatible(settings)
  override fun runConfigurationChanged(settings: RunnerAndConfigurationSettings): Unit = updateIfTfCompatible(settings)

  private fun updateIfTfCompatible(settings: RunnerAndConfigurationSettings) {
    if (settings.type is TfToolConfigurationTypeBase) {
      refreshFileGutters()
    }
  }

  private fun refreshFileGutters() {
    val psiManager = PsiManager.getInstance(project)
    val daemonAnalyzer = DaemonCodeAnalyzer.getInstance(project)

    FileEditorManager.getInstance(project).allEditors
      .mapNotNull { it.file?.takeIf { file -> isTerraformCompatibleExtension(file.extension) } }
      .mapNotNull { psiManager.findFile(it) }
      .forEach { daemonAnalyzer.restart(it) }
  }
}