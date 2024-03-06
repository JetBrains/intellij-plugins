// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.devtools

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ColoredProcessHandler
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
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture

@Service(Service.Level.PROJECT)
class DartDevToolsService(private val myProject: Project) : Disposable {
  private lateinit var processHandler: ColoredProcessHandler

  private val devToolsFuture: CompletableFuture<DartDevToolsInstance> = CompletableFuture()

  fun startService(dtdUri: String? = null) {
    val sdk = DartSdk.getDartSdk(myProject)?.takeIf { isDartSdkVersionSufficient(it) } ?: return

    val commandLine = GeneralCommandLine().withWorkDirectory(sdk.homePath)
    commandLine.charset = StandardCharsets.UTF_8
    commandLine.exePath = FileUtil.toSystemDependentName("dart")
    commandLine.addParameter("devtools")
    commandLine.addParameter("--machine")
    dtdUri?.let { commandLine.addParameter("--dtd-uri=$it") }

    processHandler = object : ColoredProcessHandler(commandLine) {
      override fun readerOptions(): BaseOutputReader.Options = BaseOutputReader.Options.forMostlySilentProcess()
    }

    processHandler.addProcessListener(object : ProcessListener {
      override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
        // The first line of text is the command issued, which can be ignored.
        val text = event.text.trim().takeUnless { it.contains(" devtools --machine") }
                   ?: return

        event.processHandler.removeProcessListener(this)

        val json: JsonObject = try {
          JsonParser.parseString(text) as JsonObject
        }
        catch (e: Exception) {
          logger<DartDevToolsService>().warn("Failed to parse message, error: ${e.message}, message: $text")
          return
        }

        val method = json["method"]?.asString
        if (method == "server.started") {
          val params = json["params"].asJsonObject
          val host: String = params.get("host").asString
          val port: Int = params.get("port").asInt

          if (port != -1) {
            devToolsFuture.complete(DartDevToolsInstance(host, port))
          }
          else {
            devToolsFuture.completeExceptionally(Exception("DevTools port was invalid"))
          }
        }
      }
    })

    processHandler.startNotify()
  }

  fun getDevToolsInstance(): DartDevToolsInstance = devToolsFuture.get()

  override fun dispose() {
    if (::processHandler.isInitialized && !processHandler.isProcessTerminated) {
      processHandler.killProcess()
    }
  }

  private fun isDartSdkVersionSufficient(sdk: DartSdk): Boolean =
    StringUtil.compareVersionNumbers(sdk.version, MIN_SDK_VERSION) >= 0


  companion object {
    fun getInstance(project: Project): DartDevToolsService = project.service()

    private const val MIN_SDK_VERSION: String = "2.15"
  }
}
