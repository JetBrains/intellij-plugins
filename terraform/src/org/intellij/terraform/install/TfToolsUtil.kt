// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.install

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.execution.process.CapturingProcessAdapter
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.SystemInfoRt
import com.intellij.util.io.HttpRequests
import com.intellij.util.system.CpuArch
import kotlinx.coroutines.ensureActive
import org.intellij.terraform.config.util.TfExecutor
import org.intellij.terraform.config.util.executeSuspendable
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.opentofu.runtime.OpenTofuProjectSettings
import org.intellij.terraform.runtime.TfProjectSettings
import org.intellij.terraform.runtime.TfToolSettings
import org.jetbrains.annotations.Nls
import kotlin.coroutines.coroutineContext

internal enum class TfToolType(@Nls val executableName: String) {
  TERRAFORM("terraform") {
    override val displayName = "Terraform"
    override fun getDownloadUrl(): String {
      val latestStableVersion = fetchLatestStableVersion(TERRAFORM_VERSION_URL) ?: DEFAULT_TERRAFORM_VERSION
      return "$downloadServerUrl/$latestStableVersion/terraform_${latestStableVersion}_${getOSName()}_${getArchName()}.zip"
    }

    override val downloadServerUrl: String
      get() = "https://releases.hashicorp.com/terraform"

    override fun getToolSettings(project: Project): TfToolSettings {
      return project.service<TfProjectSettings>()
    }

    private fun fetchLatestStableVersion(apiUrl: String): String? {
      return try {
        val response = HttpRequests.request(apiUrl).readString()
        val jsonNode = ObjectMapper().readTree(response)
        jsonNode.get("current_version")?.asText()
      }
      catch (e: Exception) {
        logger<BinaryInstaller>().error("Failed to fetch the latest stable Terraform version", e)
        null
      }
    }
  },
  OPENTOFU("tofu") {
    override val displayName = "OpenTofu"
    override fun getDownloadUrl(): String {
      return downloadServerUrl
    }

    override val downloadServerUrl: String
      get() = "" //"https://get.opentofu.org/tofu/api.json"

    override fun getToolSettings(project: Project): TfToolSettings {
      return project.service<OpenTofuProjectSettings>()
    }
  };

  fun getBinaryName(): String = getBinaryName(executableName)

  abstract fun getDownloadUrl(): String
  abstract val downloadServerUrl: String
  abstract fun getToolSettings(project: Project): TfToolSettings
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

internal fun getBinaryName(executableName: String): String {
  return if (SystemInfoRt.isWindows)
    "$executableName.exe"
  else
    executableName
}

internal fun installTFTool(
  project: Project,
  resultHandler: (InstallationResult) -> Unit = {},
  progressIndicator: ProgressIndicator? = null,
  type: TfToolType,
) {
  BinaryInstaller.create(project)
    .withBinaryName { type.getBinaryName() }
    .withDownloadUrl { type.getDownloadUrl() }
    .withProgressIndicator(progressIndicator)
    .withResultHandler { result -> resultHandler(result) }
    .install()
}

internal suspend fun getToolVersion(project: Project, tool: TfToolType, exePath: String? = tool.executableName): @NlsSafe String {
  val capturingProcessAdapter = CapturingProcessAdapter()

  val success = TfExecutor.`in`(project, tool)
    .withExePath(exePath)
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

private const val DEFAULT_TERRAFORM_VERSION: String = "1.10.0"
private const val TERRAFORM_VERSION_URL: String = "https://checkpoint-api.hashicorp.com/v1/check/terraform"