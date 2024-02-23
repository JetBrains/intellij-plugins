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
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.jetbrains.lang.dart.sdk.DartSdk
import io.flutter.run.daemon.DevToolsInstance
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture

@Service(Service.Level.PROJECT)
class DevToolsService(private val myProject: Project) : Disposable {
  private val MIN_SDK_VERSION: String = "2.15"
  private lateinit var processHandler: ColoredProcessHandler;

  private val devToolsFuture: CompletableFuture<DevToolsInstance> = CompletableFuture()

  fun startService(dtdUri: String? = null, dtdKey: String? = null): Void? {
    val sdk = DartSdk.getDartSdk(myProject)
    if (sdk == null || !isDartSdkVersionSufficient(sdk)) {
      return null
    }
    val result = GeneralCommandLine().withWorkDirectory(sdk.homePath)
    result.charset = StandardCharsets.UTF_8
    result.exePath = FileUtil.toSystemDependentName("dart")
    result.addParameter("devtools")
    result.addParameter("--machine")
    if (dtdUri != null) {
      result.addParameter("--dtd-uri=$dtdUri")
    }
    if (dtdKey !=  null) {
      result.addParameter("--dtd-key=$dtdKey")
    }

    processHandler = ColoredProcessHandler(result)
    processHandler.addProcessListener(object : ProcessListener {
      override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
        val text = event.text.trim { it <= ' ' }
        val json: JsonObject
        try {
          json = JsonParser.parseString(text) as JsonObject
        }
        catch (e: Exception) {
          println("Parse message failed: $text $e")
          return
        }

        val method = json["method"]?.asString
        if (method == "server.started") {
          val params = json["params"].asJsonObject
          val host: String = params.get("host").asString
          val port: Int = params.get("port").asInt

          if (port != -1) {
            devToolsFuture.complete(DevToolsInstance(host, port))
          }
          else {
            devToolsFuture.completeExceptionally(Exception("DevTools port was invalid"))
          }
        }

      }
    })

    processHandler.startNotify()
    return null
  }

  fun getDevToolsInstance(): DevToolsInstance {
    return devToolsFuture.get()
  }

  override fun dispose() {
    TODO("Not yet implemented")
  }

  private fun isDartSdkVersionSufficient(sdk: DartSdk): Boolean {
    return StringUtil.compareVersionNumbers(sdk.version, MIN_SDK_VERSION) >= 0
  }
}