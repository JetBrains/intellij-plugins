package com.jetbrains.cidr.cpp.embedded.platformio.project

import com.intellij.execution.ExecutionException
import com.intellij.execution.ExecutionTargetManager
import com.intellij.execution.Platform
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.execution.configurations.PtyCommandLine
import com.intellij.ide.impl.isTrusted
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.asSafely
import com.jetbrains.cidr.cpp.embedded.platformio.*
import java.nio.file.Path

private const val POSIX_HOME_ENV_VAR_NAME = "HOME"
private const val WIN_HOME_ENV_VAR_NAME = "USERPROFILE"
const val ENV_PATH = "PATH"

class PlatformioCliBuilder(
  usePty:Boolean,
  private val project: Project?,
  private var useEnvName: Boolean = false,
  private var verboseAllowed: Boolean = true
) {
  private val commandLine: GeneralCommandLine

  init {
    if (project?.isTrusted() == false) {
      throw ExecutionException(ClionEmbeddedPlatformioBundle.message("project.not.trusted"))
    }
    commandLine = if(usePty) PtyCommandLine() else GeneralCommandLine()
  }

  fun withParams(vararg params: String): PlatformioCliBuilder {
    commandLine.withParameters(*params)
    return this
  }

  fun withParams(params: List<String>): PlatformioCliBuilder {
    commandLine.withParameters(params)
    return this
  }

  @Suppress("unused")
  fun withEnvName(appendKeys: Boolean = true): PlatformioCliBuilder {
    useEnvName = appendKeys
    return this
  }

  fun withVerboseAllowed(verboseAllowed: Boolean = true): PlatformioCliBuilder {
    this.verboseAllowed = verboseAllowed
    return this
  }


  @Throws(ExecutionException::class)
  fun build(): GeneralCommandLine {
    val service = project?.service<PlatformioService>()
    if (verboseAllowed && service?.verbose == true) commandLine.withParameters("-v")
    commandLine.exePath = PlatformioConfigurable.pioExePath()

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
      commandLine.withEnvironment(ENV_PATH, path + Platform.current().pathSeparator + pioBinFolder.toAbsolutePath())
    }
    commandLine.withWorkDirectory(project?.basePath ?: FileUtil.getTempDirectory())
    return commandLine
  }

  fun withGdbHomeCompatibility(): PlatformioCliBuilder {
    if (commandLine.effectiveEnvironment[POSIX_HOME_ENV_VAR_NAME] == null) {
      commandLine.withEnvironment(POSIX_HOME_ENV_VAR_NAME, commandLine.effectiveEnvironment[WIN_HOME_ENV_VAR_NAME]!!)
    }
    return this
  }

  fun withRedirectErrorStream(q: Boolean): PlatformioCliBuilder {
    commandLine.withRedirectErrorStream(q)
    return this
  }

}