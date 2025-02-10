// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.components.service
import com.intellij.openapi.options.ConfigurationQuickFix
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.platform.eel.provider.asNioPath
import com.intellij.platform.eel.provider.getEelDescriptor
import com.intellij.platform.eel.provider.utils.where
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.platform.ide.progress.withBackgroundProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.intellij.terraform.config.TerraformConstants
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.install.TfToolType
import java.nio.file.Path
import kotlin.io.path.*

internal interface ToolPathDetector {

  companion object {
    fun getInstance(project: Project): ToolPathDetector = project.service<ToolPathDetector>()
  }

  suspend fun detectAndVerifyTool(toolType: TfToolType, overrideExistingValue: Boolean): Boolean

  fun isExecutable(path: Path): Boolean

  suspend fun detect(path: String): String?
}

internal class ToolPathDetectorImpl(val project: Project, val coroutineScope: CoroutineScope) : ToolPathDetector {

  override suspend fun detectAndVerifyTool(toolType: TfToolType, overwriteExistingSettings: Boolean): Boolean {
    val toolPath = Path(toolType.getToolSettings(project).toolPath)
    if (isExecutable(toolPath)) return true
    if (overwriteExistingSettings || toolPath.pathString.isBlank()) {
      withBackgroundProgress(project, HCLBundle.message("progress.title.detecting.terraform.executable", toolType.displayName)) {
        detectToolAndUpdateSettings(toolType)
      }
    }
    return isExecutable(Path(toolType.getToolSettings(project).toolPath))
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
      if (isExecutable(filePath)) {
        return@withContext path
      }
      val fileName = filePath.fileName.nameWithoutExtension
      val eelApi = project.getEelDescriptor().upgrade()
      val exePath = eelApi.exec.where(fileName)?.asNioPath()?.takeIf { isExecutable(it) }?.absolutePathString()
      return@withContext exePath
    }
  }

  override fun isExecutable(path: Path): Boolean {
    return path.pathString.isNotBlank() && path.isRegularFile() && path.isExecutable()
  }
}

internal fun showIncorrectPathNotification(
  project: Project,
  toolType: TfToolType,
) {
  val toolPath = toolType.getToolSettings(project).toolPath
  TerraformConstants.getNotificationGroup().createNotification(
    HCLBundle.message("run.configuration.terraform.path.title", toolType.displayName),
    HCLBundle.message("run.configuration.terraform.path.incorrect", toolPath.ifEmpty { toolType.executableName }, toolType.displayName),
    NotificationType.ERROR
  ).addActions(setOf(DetectExecutableAction(project, toolType), OpenSettingsAction()))
    .notify(project)
}

internal fun showDetectedPathNotification(project: Project, toolType: TfToolType) {
  val detectedPath = toolType.getToolSettings(project).toolPath
  TerraformConstants.getNotificationGroup().createNotification(
    HCLBundle.message("run.configuration.terraform.path.detected.title", toolType.displayName),
    HCLBundle.message("run.configuration.terraform.path.detected", detectedPath),
    NotificationType.INFORMATION
  ).addAction(OpenSettingsAction()).notify(project)
}

internal class DetectExecutableAction(
  private val project: Project,
  private val toolType: TfToolType,
) : NotificationAction(HCLBundle.message("tool.detectAndTestButton.text")), ConfigurationQuickFix {

  override fun actionPerformed(e: AnActionEvent, notification: Notification) {
    notification.expire()
    detectExecutable(true)
  }

  override fun applyFix(dataContext: DataContext) {
    detectExecutable(false)
  }

  private fun detectExecutable(showNotification: Boolean = true) {
    val isToolSetUp = runWithModalProgressBlocking(project, HCLBundle.message("progress.title.detecting.terraform.executable", toolType.displayName)) {
      ToolPathDetector.getInstance(project).detectAndVerifyTool(toolType, true)
    }
    if (isToolSetUp && showNotification) {
      showDetectedPathNotification(project, toolType)
    }
    else if (showNotification) {
      showIncorrectPathNotification(project, toolType)
    }
  }
}

internal class OpenSettingsAction : NotificationAction(HCLBundle.message("terraform.open.settings")) {
  override fun actionPerformed(e: AnActionEvent, notification: Notification) {
    notification.expire()
    ShowSettingsUtil.getInstance().showSettingsDialog(e.project, TerraformToolConfigurable::class.java)
  }
}

