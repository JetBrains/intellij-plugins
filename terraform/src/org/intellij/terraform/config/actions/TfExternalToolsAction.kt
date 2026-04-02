// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.actions

import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.isFile
import com.intellij.platform.ide.progress.withBackgroundProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.intellij.terraform.config.TfConstants
import org.intellij.terraform.config.util.getApplicableToolType
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.isTfOrTofuFile
import org.intellij.terraform.runtime.TfToolPathDetector
import org.intellij.terraform.runtime.showIncorrectPathNotification
import org.jetbrains.annotations.Nls
import kotlin.coroutines.cancellation.CancellationException

internal fun isTfOrTofuAvailable(file: VirtualFile): Boolean {
  if (!file.isInLocalFileSystem) return false

  if (file.isDirectory) {
    return file.children?.any { it.isFile && isTfOrTofuFile(it) } ?: false
  }
  return isTfOrTofuFile(file)
}

internal abstract class TfExternalToolsAction : DumbAwareAction() {

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    val project = e.project
    val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
    e.presentation.isEnabledAndVisible = project != null &&
                                         file != null &&
                                         isTfOrTofuAvailable(file)
  }

  protected fun getActionCoroutineScope(project: Project): CoroutineScope = project.service<CoroutineScopeProvider>().coroutineScope

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
    val title = e.presentation.text ?: HCLBundle.message("progress.title.processing")

    getActionCoroutineScope(project).launch {
      try {
        val toolType = getApplicableToolType(file)
        val isToolConfigured =
          withBackgroundProgress(project, title) { TfToolPathDetector.getInstance(project).detectAndVerifyTool(toolType, false) }
        if (!isToolConfigured) {
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
