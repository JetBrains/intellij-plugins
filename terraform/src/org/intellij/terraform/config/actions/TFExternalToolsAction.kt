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

import com.intellij.execution.wsl.WslPath
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.intellij.terraform.config.TerraformConstants
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.HCLFileType
import org.intellij.terraform.install.TfToolType
import org.intellij.terraform.runtime.TerraformToolConfigurable
import org.intellij.terraform.runtime.ToolPathDetector
import org.jetbrains.annotations.Nls
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

internal abstract class TFExternalToolsAction : DumbAwareAction() {

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
    val title = e.presentation.text ?: "Processing..."

    getActionCoroutineScope(project).launch {
      try {
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
    private val LOG = Logger.getInstance(TFExternalToolsAction::class.java)
  }

  @Service(Service.Level.PROJECT)
  private class CoroutineScopeProvider(val coroutineScope: CoroutineScope)
}

internal fun notifyError(title: @Nls String, project: Project, ex: Throwable?) {
  TerraformConstants.EXECUTION_NOTIFICATION_GROUP
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

internal fun isExecutableToolFileConfigured(project: Project, toolType: TfToolType): Boolean {
  val toolPath = toolType.getToolSettings(project).toolPath
  return if (!isPathExecutable(toolPath)) {
    TerraformConstants.EXECUTION_NOTIFICATION_GROUP.createNotification(
      HCLBundle.message("run.configuration.terraform.path.title", toolType.displayName),
      HCLBundle.message("run.configuration.terraform.path.incorrect", toolPath.ifEmpty { toolType.executableName }, toolType.displayName),
      NotificationType.ERROR
    ).addAction(object : NotificationAction(HCLBundle.message("terraform.open.settings")) {
      override fun actionPerformed(e: AnActionEvent, notification: Notification) {
        ShowSettingsUtil.getInstance().showSettingsDialog(e.project, TerraformToolConfigurable::class.java)
      }
    }).notify(project)
    false
  }
  else {
    true
  }
}

internal fun isPathExecutable(path: String): Boolean {
  if (FileUtil.canExecute(File(path))) return true
  return WslPath.parseWindowsUncPath(path) != null
}