// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.devtools

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.KillableProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.io.BaseOutputReader
import com.jetbrains.lang.dart.sdk.DartSdk
import com.jetbrains.lang.dart.sdk.DartSdkUtil
import java.nio.charset.StandardCharsets

@Service(Service.Level.PROJECT)
class DartDevToolsService(private val myProject: Project) : Disposable {

  private lateinit var processHandler: KillableProcessHandler
  private var serviceRunning = false

  var devToolsHostAndPort: String? = null
    private set

  fun startService(dtdUri: String? = null) {
    if (serviceRunning) {
      logger.error("Ignoring startService() call. Dart Tooling Daemon is already running.")
      return
    }

    val sdk = DartSdk.getDartSdk(myProject)?.takeIf { isDartSdkVersionSufficient(it) } ?: return

    val commandLine = GeneralCommandLine().withWorkDirectory(sdk.homePath)
    commandLine.charset = StandardCharsets.UTF_8
    commandLine.exePath = FileUtil.toSystemDependentName(DartSdkUtil.getDartExePath(sdk))
    commandLine.addParameter("devtools")
    commandLine.addParameter("--machine")
    dtdUri?.let { commandLine.addParameter("--dtd-uri=$it") }

    logger.info("Starting Dart DevTools, sdk ${sdk.version}")
    processHandler = object : KillableProcessHandler(commandLine) {
      override fun readerOptions(): BaseOutputReader.Options = BaseOutputReader.Options.forMostlySilentProcess()
    }
    processHandler.addProcessListener(DevToolsProcessListener())
    processHandler.startNotify()
  }

  override fun dispose() {
    if (::processHandler.isInitialized && !processHandler.isProcessTerminated) {
      processHandler.killProcess()
    }
  }

  private fun isDartSdkVersionSufficient(sdk: DartSdk): Boolean =
    StringUtil.compareVersionNumbers(sdk.version, MIN_SDK_VERSION) >= 0


  private inner class DevToolsProcessListener : ProcessListener {
    override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
      logger.debug("DevTools output: ${event.text}")
      if (serviceRunning) return

      // The first line of text is the command issued, which can be ignored.
      val text = event.text.trim().takeUnless { it.contains(" devtools --machine") }
                 ?: return

      try {
        val json = JsonParser.parseString(text) as JsonObject
        val method = json["method"]?.asString
        if (method == "server.started") {
          serviceRunning = true
          val params = json["params"].asJsonObject
          val host: String = params.get("host").asString
          val port: Int = params.get("port").asInt
          devToolsHostAndPort = "$host:$port"
        }
      }
      catch (e: Exception) {
        logger.warn("Failed to parse message, error: ${e.message}, message: $text")
        return
      }
    }

    override fun processTerminated(event: ProcessEvent) {
      serviceRunning = false
      devToolsHostAndPort = null
      logger.info("DevTools terminated, exit code: ${event.exitCode}")
    }
  }


  companion object {
    @JvmStatic
    fun getInstance(project: Project): DartDevToolsService = project.service()

    private const val MIN_SDK_VERSION: String = "2.15"
    private val logger = logger<DartDevToolsService>()
  }
}
