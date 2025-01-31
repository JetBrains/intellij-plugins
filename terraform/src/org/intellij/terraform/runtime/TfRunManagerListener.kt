// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.execution.RunManagerListener
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import org.intellij.terraform.isTerraformFileExtension

private class TfRunManagerListener(val project: Project) : RunManagerListener {

  override fun runConfigurationAdded(settings: RunnerAndConfigurationSettings) = updateIfTerraform(settings)
  override fun runConfigurationRemoved(settings: RunnerAndConfigurationSettings) = updateIfTerraform(settings)
  override fun runConfigurationChanged(settings: RunnerAndConfigurationSettings) = updateIfTerraform(settings)

  private fun updateIfTerraform(settings: RunnerAndConfigurationSettings) {
    if (settings.type is TfToolConfigurationTypeBase) {
      updateGutterOfFiles()
    }
  }

  private fun updateGutterOfFiles() {
    val psiManager = PsiManager.getInstance(project)
    val daemonAnalyzer = DaemonCodeAnalyzer.getInstance(project)

    FileEditorManager.getInstance(project).allEditors
      .mapNotNull { it.file?.takeIf { file -> isTerraformFileExtension(file.extension) } }
      .mapNotNull { psiManager.findFile(it) }
      .forEach { daemonAnalyzer.restart(it) }
  }
}