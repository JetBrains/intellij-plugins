// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.install

import com.intellij.execution.process.CapturingProcessAdapter
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.SystemInfoRt
import com.intellij.util.system.CpuArch
import kotlinx.coroutines.ensureActive
import org.intellij.terraform.config.util.TFExecutor
import org.intellij.terraform.config.util.executeSuspendable
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.opentofu.runtime.OpenTofuPathDetector
import org.intellij.terraform.runtime.TerraformPathDetector
import org.intellij.terraform.runtime.ToolPathDetector
import org.intellij.terraform.runtime.ToolSettings
import org.jetbrains.annotations.Nls
import kotlin.coroutines.coroutineContext

val LOG: Logger = logger<InstallTerraformAction>()

enum class TfToolType(@Nls val executableName: String) {
  TERRAFORM("terraform") {
    override val displayName = "Terraform"
    override fun getDownloadUrl(): String {
      val latestStableVersion = "1.8.0"
      return "$downloadServerUrl/$latestStableVersion/terraform_${latestStableVersion}_${getOSName()}_${getArchName()}.zip"
    }

    override val downloadServerUrl: String
      get() = "https://releases.hashicorp.com/terraform"
    override fun getPathDetector(project: Project): ToolPathDetector {
      return project.service<TerraformPathDetector>()
    }
  },
  OPENTOFU("tofu") {
    override val displayName = "OpenTofu"
    override fun getDownloadUrl(): String {
      return downloadServerUrl
    }

    override val downloadServerUrl: String
      get() = "" //"https://get.opentofu.org/tofu/api.json"

    override fun getPathDetector(project: Project): ToolPathDetector {
      return project.service<OpenTofuPathDetector>()
    }
  };

  fun getBinaryName(): String {
    return if (SystemInfoRt.isWindows)
      "${executableName}.exe"
    else
      executableName
  }

  abstract fun getDownloadUrl(): String
  abstract val downloadServerUrl: String
  abstract fun getPathDetector(project: Project): ToolPathDetector
  abstract val displayName: String

  protected fun getOSName(): String? {
    return when {
      SystemInfoRt.isWindows -> "windows"
      SystemInfoRt.isLinux -> "linux"
      SystemInfoRt.isMac -> "darwin"
      else -> null
    }
  }

  protected fun getArchName(): String {
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
}

internal fun installTFTool(
  project: Project,
  resultHandler: (Boolean) -> Unit = {},
  progressIndicator: ProgressIndicator? = null,
  type: TfToolType,
  toolSettings: ToolSettings,
) {
  BinaryInstaller.create(project)
    .withBinaryName { type.getBinaryName() }
    .withDownloadUrl { type.getDownloadUrl() }
    .withProgressIndicator(progressIndicator)
    .withResultHandler { result ->
      if (result is SuccessfulInstallation) {
        // TODO - update textField
        toolSettings.toolPath = result.binary.toAbsolutePath().toString()
      }
      resultHandler(result is SuccessfulInstallation)
    }
    .install()
}

internal suspend fun getToolVersion(project: Project, tool: TfToolType): @NlsSafe String {
  val capturingProcessAdapter = CapturingProcessAdapter()

  val success = TFExecutor.`in`(project, tool)
    .withPresentableName(HCLBundle.message("tool.executor.version", tool.displayName))
    .withParameters("version")
    .withPassParentEnvironment(true)
    .withProcessListener(capturingProcessAdapter)
    .executeSuspendable()

  coroutineContext.ensureActive()

  val stdout = capturingProcessAdapter.output.stdout
  if (!success || stdout.isEmpty()) {
    throw RuntimeException("Couldn't get version of ${tool.displayName}")
  }
  return stdout
}