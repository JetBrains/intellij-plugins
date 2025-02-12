/*
 * Copyright 2013-2016 Sergey Ignatov, Alexander Zolotov, Florin Patan
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
package org.intellij.terraform.config.actions

import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.intellij.terraform.config.TfConstants
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.config.util.getApplicableToolType
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.HCLFileType
import org.intellij.terraform.runtime.ToolPathDetector
import org.intellij.terraform.runtime.showIncorrectPathNotification
import org.intellij.terraform.install.TfToolType
import org.intellij.terraform.runtime.TfToolConfigurable
import org.jetbrains.annotations.Nls
import kotlin.coroutines.cancellation.CancellationException

internal abstract class TfExternalToolsAction : DumbAwareAction() {

  private fun isAvailableOnFile(file: VirtualFile, checkDirChildren: Boolean, onlyTerraformFileType: Boolean): Boolean {
    if (!file.isInLocalFileSystem) return false
    if (file.isDirectory) {
      if (!checkDirChildren) return false
      val children = file.children
      if (children != null) {
        for (child in children) {
          if (isAvailableOnFile(child, false, onlyTerraformFileType)) return true
        }
      }
      return false
    }

    return FileTypeRegistry.getInstance().run {
      if (onlyTerraformFileType) isFileOfType(file, TerraformFileType)
      else isFileOfType(file, HCLFileType) || isFileOfType(file, TerraformFileType)
    }
  }

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    val project = e.project
    val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
    if (project == null || file == null || !isAvailableOnFile(file, true, true)) {
      e.presentation.isEnabled = false
      return
    }
    e.presentation.isEnabled = true
  }

  protected fun getActionCoroutineScope(project: Project): CoroutineScope = project.service<CoroutineScopeProvider>().coroutineScope

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
    val title = e.presentation.text ?: HCLBundle.message("progress.title.processing")

    getActionCoroutineScope(project).launch {
      try {
        val toolType = getApplicableToolType(file)
        if (!ToolPathDetector.getInstance(project).detectAndVerifyTool(toolType, false)) {
          showIncorrectPathNotification(project, toolType)
          return@launch
        }
        invoke(project, title, file)
      }
      catch (ex: Exception) {
        if (ex is CancellationException) throw ex
        notifyError(title, project, ex)
        LOG.error(ex)
      }
    }
  }

  abstract suspend fun invoke(project: Project, title: @Nls String, vararg virtualFiles: VirtualFile)

  companion object {
    private val LOG = Logger.getInstance(TfExternalToolsAction::class.java)
  }

  @Service(Service.Level.PROJECT)
  private class CoroutineScopeProvider(val coroutineScope: CoroutineScope)
}

internal fun notifyError(title: @Nls String, project: Project, ex: Throwable?) {
  TfConstants.getNotificationGroup()
    .createNotification(
      title,
      @Suppress("HardCodedStringLiteral")
      generateSequence(ex) { it.cause }
        .mapNotNull { it.message }
        .distinct()
        .joinToString("\n"),
      NotificationType.ERROR
    ).notify(project)
}
