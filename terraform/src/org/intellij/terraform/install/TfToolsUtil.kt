// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.install

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.SystemInfoRt
import com.intellij.platform.eel.ExecuteProcessException
import com.intellij.platform.eel.provider.getEelDescriptor
import com.intellij.platform.eel.provider.utils.readWholeText
import com.intellij.platform.eel.spawnProcess
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread
import com.intellij.util.io.HttpRequests
import com.intellij.util.system.CpuArch
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.opentofu.runtime.OpenTofuProjectSettings
import org.intellij.terraform.runtime.TfProjectSettings
import org.intellij.terraform.runtime.TfToolSettings
import org.intellij.terraform.terragrunt.runtime.TerragruntProjectSettings
import org.jetbrains.annotations.Nls
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths

internal enum class TfToolType(@param:Nls val executableName: String) {
  TERRAFORM("terraform") {
    override val displayName = "Terraform"
    override suspend fun getDownloadUrl(): String {
      val latestTfVersion = fetchTfLatestStableVersion() ?: DEFAULT_TERRAFORM_VERSION
      return "$downloadServerUrl/$latestTfVersion/terraform_${latestTfVersion}_${getOSName()}_${getArchName()}.zip"
    }

    override fun getToolSettings(project: Project): TfToolSettings {
      return project.service<TfProjectSettings>()
    }

    private val downloadServerUrl: String
      get() = "https://releases.hashicorp.com/terraform"

    private fun fetchTfLatestStableVersion(): String? {
      return try {
        val response = HttpRequests.request(TERRAFORM_VERSION_URL).readString()
        val jsonNode = ObjectMapper().readTree(response)
        jsonNode.get("current_version")?.asText()?.removePrefix("v")
      }
      catch (e: Exception) {
        logger<TfBinaryInstaller>().error("Failed to fetch the latest stable Terraform version", e)
        null
      }
    }
  },
  OPENTOFU("tofu") {
    override val displayName = "OpenTofu"
    override suspend fun getDownloadUrl(): String {
      val latestTofuVersion = fetchTofuLatestStableVersion() ?: DEFAULT_OPENTOFU_VERSION
      return "$downloadServerUrl/v$latestTofuVersion/tofu_${latestTofuVersion}_${getOSName()}_${getArchName()}.zip"
    }

    override fun getToolSettings(project: Project): TfToolSettings {
      return project.service<OpenTofuProjectSettings>()
    }

    private val downloadServerUrl: String
      get() = "https://github.com/opentofu/opentofu/releases/download"

    private fun fetchTofuLatestStableVersion(): String? {
      return try {
        val response = HttpRequests.request(OPENTOFU_VERSION_URL).readString()
        val jsonNode = ObjectMapper().readTree(response)
        jsonNode.get("versions")?.first()?.get("id")?.asText()
      }
      catch (e: Exception) {
        logger<TfBinaryInstaller>().error("Failed to fetch the latest stable OpenTofu version", e)
        null
      }
    }
  },
  TERRAGRUNT("terragrunt") {
    override val displayName: String = "Terragrunt"
    override suspend fun getDownloadUrl(): String {
      val latestTerragruntVersion = fetchTerragruntStableVersion() ?: DEFAULT_TERRAGRUNT_VERSION
      return "$downloadServerUrl/$latestTerragruntVersion/terragrunt_${getOSName()}_${getArchName()}${if (getOSName() == "windows") ".exe" else ""}"
    }

    override fun getToolSettings(project: Project): TfToolSettings {
      return project.service<TerragruntProjectSettings>()
    }

    private val downloadServerUrl: String
      get() = "https://github.com/gruntwork-io/terragrunt/releases/download"

    private fun fetchTerragruntStableVersion(): String? {
      return try {
        val response = HttpRequests.request(TERRAGRUNT_VERSION_URL).readString()
        val jsonNode = ObjectMapper().readTree(response)
        jsonNode.get("tag_name")?.asText()
      }
      catch (e: Exception) {
        logger<TfBinaryInstaller>().error("Failed to fetch the latest stable Terragrunt version", e)
        null
      }
    }
  };

  fun getBinaryName(): String = getBinaryName(executableName)

  abstract val displayName: String
  abstract fun getToolSettings(project: Project): TfToolSettings

  @RequiresBackgroundThread
  abstract suspend fun getDownloadUrl(): String

  protected fun getOSName(): String = when {
    SystemInfoRt.isWindows -> "windows"
    SystemInfoRt.isLinux -> "linux"
    SystemInfoRt.isMac -> "darwin"
    SystemInfoRt.isFreeBSD -> "freebsd"
    else -> ""
  }

  protected fun getArchName(): String = when (CpuArch.CURRENT) {
    CpuArch.X86 -> "386"
    CpuArch.X86_64 -> "amd64"
    CpuArch.ARM32 -> "arm"
    CpuArch.ARM64 -> "arm64"
    else -> ""
  }

  open fun getInstallationDirectory(): Path? {
    return if (SystemInfoRt.isWindows) {
      val binaryFolderName = getBinaryName().substringBefore('.').takeIf { it.isNotEmpty() } ?: return null
      "${System.getProperty("user.home")}/.jetbrains/$binaryFolderName/"
        .let(::toSystemIndependentName)
        .let(Paths::get)
    }
    else {
      Paths.get("/usr/local/bin/")
    }
  }

  private fun toSystemIndependentName(filePath: String): String {
    val pathSeparator = FileSystems.getDefault().separator
    return filePath.replace(pathSeparator, "/")
  }
}

internal fun getBinaryName(executableName: String): String {
  return if (SystemInfoRt.isWindows)
    "$executableName.exe"
  else
    executableName
}

internal suspend fun getToolVersion(project: Project, tool: TfToolType, exePath: String): @NlsSafe String {
  val eelApi = project.getEelDescriptor().toEelApi()
  val envVariables = eelApi.exec.fetchLoginShellEnvVariables()
  val processBuilder = eelApi.exec.spawnProcess(exePath)
    .args("--version")
    .env(envVariables)

  val version = try {
    processBuilder.eelIt().stdout.readWholeText()
  }
  catch (e: ExecuteProcessException) {
    throw e
  }

  if (version.isEmpty())
    throw RuntimeException(HCLBundle.message("tool.executor.version.error", tool.displayName))
  return version
}

private const val DEFAULT_TERRAFORM_VERSION: String = "1.13.0"
private const val TERRAFORM_VERSION_URL: String = "https://checkpoint-api.hashicorp.com/v1/check/terraform"

private const val DEFAULT_OPENTOFU_VERSION: String = "1.10.5"
private const val OPENTOFU_VERSION_URL: String = "https://get.opentofu.org/tofu/api.json"

private const val DEFAULT_TERRAGRUNT_VERSION: String = "v0.86.2"
private const val TERRAGRUNT_VERSION_URL: String = "https://api.github.com/repos/gruntwork-io/terragrunt/releases/latest"