package com.jetbrains.cidr.cpp.embedded.platformio.project

import com.intellij.execution.ExecutionException
import com.intellij.execution.ExecutionTargetManager
import com.intellij.execution.Platform
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.ide.impl.isTrusted
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.asSafely
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioConfigurable
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioConfigurable.Companion.pioExePath
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioService
import java.nio.file.Path

private const val POSIX_HOME_ENV_VAR_NAME = "HOME"
private const val WIN_HOME_ENV_VAR_NAME = "USERPROFILE"
const val ENV_PATH = "PATH"

class PlatfromioCliBuilder(
  private val project: Project?,
  private var useEnvName: Boolean = false,
  private var verboseAllowed: Boolean = true
) {
  private val commandLine: GeneralCommandLine

  init {
    if (project?.isTrusted() == false) {
      throw ExecutionException(ClionEmbeddedPlatformioBundle.message("project.not.trusted"))
    }
    commandLine = GeneralCommandLine()
  }

  fun withParams(vararg params: String): PlatfromioCliBuilder {
    commandLine.withParameters(*params)
    return this
  }

  fun withParams(params: List<String>): PlatfromioCliBuilder {
    commandLine.withParameters(params)
    return this
  }

  fun withEnvName(appendKeys: Boolean = true): PlatfromioCliBuilder {
    useEnvName = appendKeys
    return this
  }

  fun withVerboseAllowed(verboseAllowed: Boolean = true): PlatfromioCliBuilder {
    this.verboseAllowed = verboseAllowed
    return this
  }


  @Throws(ExecutionException::class)
  fun build(): GeneralCommandLine {
    val service = project?.service<PlatformioService>()
    if (verboseAllowed && service?.verbose == true) commandLine.withParameters("-v")
    commandLine.exePath = pioExePath()

    if (useEnvName && project != null) {
      val envName = ExecutionTargetManager.getActiveTarget(project).asSafely<PlatformioExecutionTarget>()?.id
      if (!envName.isNullOrEmpty()) {
        commandLine.withParameters("-e", envName)
      }
    }
    if (service?.isUploadPortAuto == false) {
      commandLine.environment["PLATFORMIO_UPLOAD_PORT"] = service.uploadPort
    }
    val pioBinFolder = PlatformioConfigurable.pioBinFolder()
    val path = commandLine.effectiveEnvironment.getOrDefault(ENV_PATH, "")
    if (PathEnvironmentVariableUtil.getPathDirs(path).none { pioBinFolder == Path.of(it) }) {
      commandLine.withEnvironment(ENV_PATH, path + Platform.current().pathSeparator + pioBinFolder?.toAbsolutePath())
    }
    commandLine.withWorkDirectory(project?.basePath ?: FileUtil.getTempDirectory())
    return commandLine

  }

  fun withGdbHomeCompatibility(): PlatfromioCliBuilder {
    if (commandLine.effectiveEnvironment[POSIX_HOME_ENV_VAR_NAME] == null) {
      commandLine.withEnvironment(POSIX_HOME_ENV_VAR_NAME, commandLine.effectiveEnvironment[WIN_HOME_ENV_VAR_NAME]!!)
    }
    return this
  }

  fun withRedirectErrorStream(q: Boolean): PlatfromioCliBuilder {
    commandLine.withRedirectErrorStream(q)
    return this
  }

}