// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.install

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfoRt
import com.intellij.util.system.CpuArch
import org.intellij.terraform.runtime.TerraformProjectSettings

private const val TERRAFORM_BASE_URL: String = "https://releases.hashicorp.com/terraform"
val LOG: Logger = logger<InstallTerraformAction>()

internal fun installTerraform(project: Project,
                              resultHandler: (Boolean) -> Unit = {},
                              progressIndicator: ProgressIndicator? = null) {
  BinaryInstaller.create(project)
    .withBinaryName { getBinaryName() }
    .withDownloadUrl { getDownloadUrl() }
    .withProgressIndicator(progressIndicator)
    .withResultHandler { result ->
      if (result is SuccessfulInstallation) {
        // TODO - update textField
        TerraformProjectSettings.getInstance(project).terraformPath = result.binary.toAbsolutePath().toString()
      }
      resultHandler(result is SuccessfulInstallation)
    }
    .install()
}

internal fun getBinaryName(): String {
  return if (SystemInfoRt.isWindows)
    "terraform.exe"
  else
    "terraform"
}

private fun getDownloadUrl(): String {
  // TODO - need to get the last stable version, may be - https://checkpoint-api.hashicorp.com/v1/check/terraform
  val latestStableVersion = "1.8.0"
  return "$TERRAFORM_BASE_URL/$latestStableVersion/terraform_${latestStableVersion}_${getOSName()}_${getArchName()}.zip"
}

private fun getOSName(): String? {
  return when {
    SystemInfoRt.isWindows -> "windows"
    SystemInfoRt.isLinux -> "linux"
    SystemInfoRt.isMac -> "darwin"
    else -> null
  }
}

private fun getArchName(): String  {
  /**
   * TODO
   * terraform_1.8.0_linux_386.zip
   * terraform_1.8.0_linux_amd64.zip
   * terraform_1.8.0_linux_arm.zip
   * terraform_1.8.0_linux_arm64.zip
   */
  return if (CpuArch.isArm64())
    "arm64"
  else
    "amd64"
}

internal class InstallTerraformAction : DumbAwareAction() {

  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabledAndVisible = e.project != null
  }

  override fun actionPerformed(e: AnActionEvent) {
    e.project?.let { installTerraform(it) }
  }
}