// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.platform.eel.provider.asNioPath
import com.intellij.platform.eel.provider.getEelDescriptor
import com.intellij.platform.eel.provider.toEelApi
import com.intellij.platform.eel.where
import com.intellij.platform.util.progress.withProgressText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.intellij.terraform.config.TfConstants
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.install.TfToolType
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.isExecutable
import kotlin.io.path.isRegularFile
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.pathString

internal interface TfToolPathDetector {

  companion object {
    fun getInstance(project: Project): TfToolPathDetector = project.service<TfToolPathDetector>()
  }

  suspend fun detectAndVerifyTool(toolType: TfToolType, overrideExistingValue: Boolean): Boolean = true

  suspend fun detect(path: String): String?
}

internal class TfToolPathDetectorImpl(val project: Project, val coroutineScope: CoroutineScope) : TfToolPathDetector {

  override suspend fun detectAndVerifyTool(toolType: TfToolType, overrideExistingValue: Boolean): Boolean {
    if (overrideExistingValue || toolType.getToolSettings(project).toolPath.isBlank()) {
      withProgressText(HCLBundle.message("progress.title.detecting.terraform.executable", toolType.displayName)) {
        detectToolAndUpdateSettings(toolType)
      }
    }
    return isValidExecutablePath(toolType.getToolSettings(project).toolPath)
  }

  private suspend fun detectToolAndUpdateSettings(toolType: TfToolType): TfToolSettings {
    val settings = toolType.getToolSettings(project)
    val execName = toolType.executableName
    if (execName.isNotBlank()) {
      val detectedPath = detect(execName)
      if (!detectedPath.isNullOrEmpty()) {
        settings.toolPath = detectedPath
      }
    }
    return settings
  }

  override suspend fun detect(path: String): String? {
    return withContext(Dispatchers.IO) {
      val filePath = Path(path)
      if (isValidExecutable(filePath)) {
        return@withContext path
      }
      val fileName = filePath.fileName.nameWithoutExtension
      val eelApi = project.getEelDescriptor().toEelApi()
      val exePath = eelApi.exec.where(fileName)?.asNioPath()?.takeIf { isValidExecutable(it) }?.absolutePathString()
      return@withContext exePath
    }
  }
}

internal fun showIncorrectPathNotification(
  project: Project,
  toolType: TfToolType,
) {
  val toolPath = toolType.getToolSettings(project).toolPath
  TfConstants.getNotificationGroup().createNotification(
    HCLBundle.message("run.configuration.terraform.path.title", toolType.displayName),
    HCLBundle.message("run.configuration.terraform.path.incorrect", toolPath.ifEmpty { toolType.executableName }, toolType.displayName),
    NotificationType.ERROR
  ).addAction(OpenSettingsAction()).notify(project)
}

internal class OpenSettingsAction : NotificationAction(HCLBundle.message("terraform.open.settings")) {
  override fun actionPerformed(e: AnActionEvent, notification: Notification) {
    notification.expire()
    ShowSettingsUtil.getInstance().showSettingsDialog(e.project, TfToolConfigurable::class.java)
  }
}

internal fun isValidExecutable(path: Path): Boolean = path.pathString.isNotBlank() && path.isRegularFile() && path.isExecutable()

internal fun isValidExecutablePath(path: String): Boolean {
  if (path.isBlank()) return false

  val nioPath = runCatching { Path(path) }.getOrNull() ?: return false
  return isValidExecutable(nioPath)
}