// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ide.progress.withBackgroundProgress
import org.intellij.terraform.TerraformIcons
import org.intellij.terraform.config.model.local.LocalSchemaService
import org.jetbrains.annotations.Nls

open class TFInitAction : TFExternalToolsAction() {

  override fun update(e: AnActionEvent) {
    super.update(e)
    e.presentation.icon = TerraformIcons.Terraform
  }

  override suspend fun invoke(project: Project, module: Module?, title: @Nls String, virtualFile: VirtualFile) {
    withBackgroundProgress(project, title) {
      project.service<TerraformActionService>().runTerraformInit(virtualFile, project, module, title)
    }
  }
}

class TFInitRequiredAction : TFInitAction() {

  override fun update(e: AnActionEvent) {
    super.update(e)
    if (!e.presentation.isEnabledAndVisible) {
      e.presentation.isVisible = false
      return
    }
    val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
    val project = e.project ?: return
    e.presentation.isEnabledAndVisible = project.service<LocalSchemaService>().findLockFile(file) == null
  }
}