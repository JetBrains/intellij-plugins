// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.toolingDaemon

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.intellij.execution.ExecutionException
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
import com.jetbrains.lang.dart.sdk.DartSdk
import com.jetbrains.lang.dart.sdk.DartSdkUtil
import de.roderick.weberknecht.WebSocket
import de.roderick.weberknecht.WebSocketEventHandler
import de.roderick.weberknecht.WebSocketException
import de.roderick.weberknecht.WebSocketMessage
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicInteger

@Service(Service.Level.PROJECT)
class DartToolingDaemonService private constructor(private val project: Project) : Disposable {
  private lateinit var webSocket: WebSocket
  private lateinit var dtdProcessHandler: ColoredProcessHandler

  private val nextRequestId = AtomicInteger()
  private val consumerMap: MutableMap<String, DartToolingDaemonConsumer> = mutableMapOf()
  private val listeners: MutableList<DartToolingDaemonListener> = mutableListOf()

  @Throws(ExecutionException::class)
  fun startService() {
    val sdk = DartSdk.getDartSdk(project)
    if (sdk == null || !isDartSdkVersionSufficient(sdk)) {
      return
    }

    val result = GeneralCommandLine().withWorkDirectory(sdk.homePath)
    result.exePath = FileUtil.toSystemDependentName(DartSdkUtil.getDartExePath(sdk))
    result.charset = StandardCharsets.UTF_8
    result.addParameter("tooling-daemon")

    dtdProcessHandler = ColoredProcessHandler(result)
    dtdProcessHandler.addProcessListener(object : ProcessListener {
      override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
        val text = event.text.trim()
        if (text.startsWith(STARTUP_MESSAGE_PREFIX)) {
          dtdProcessHandler.removeProcessListener(this)
          val address: String = text.substring(STARTUP_MESSAGE_PREFIX.length)
          connectToDtdWebSocket(address)
        }
      }
    })

    dtdProcessHandler.startNotify()
  }

  private fun connectToDtdWebSocket(address: String) {
    webSocket = WebSocket(URI("ws://$address/ws"))
    webSocket.eventHandler = DtdWebSocketEventHandler()
    webSocket.connect()
  }

  @Throws(WebSocketException::class)
  fun sendRequest(method: String, params: JsonObject, consumer: DartToolingDaemonConsumer) {
    val request = JsonObject()
    request.addProperty("jsonrpc", "2.0")
    request.addProperty("method", method)

    val id = nextRequestId.incrementAndGet().toString()
    request.addProperty("id", id)
    request.add("params", params)

    consumerMap[id] = consumer
    webSocket.send(request.toString())
  }

  fun addToolingDaemonListener(listener: DartToolingDaemonListener) {
    listeners.add(listener)
  }

  fun removeToolingDaemonListener(listener: DartToolingDaemonListener) {
    listeners.remove(listener)
  }

  override fun dispose() {
    if (::dtdProcessHandler.isInitialized) {
      dtdProcessHandler.killProcess()
    }
  }

  private fun isDartSdkVersionSufficient(sdk: DartSdk): Boolean =
    StringUtil.compareVersionNumbers(sdk.version, MIN_SDK_VERSION) >= 0


  private inner class DtdWebSocketEventHandler : WebSocketEventHandler {
    override fun onOpen() {
      // Fake request to make sure the tooling daemon works
      //val params = JsonObject()
      //params.addProperty("streamId", "foo_stream")
      //sendRequest("streamListen", params) { response ->
      //  println("received response from streamListen")
      //  println(response)
      //}
    }

    override fun onMessage(message: WebSocketMessage) {
      val json: JsonObject = try {
        JsonParser.parseString(message.text) as JsonObject
      }
      catch (e: Exception) {
        logger<DartToolingDaemonService>().warn("Failed to parse message, error: ${e.message}, message: ${message.text}")
        return
      }

      val method = json["method"]?.asString
      if (method == "streamNotify") {
        val params = json["params"].asJsonObject
        for (listener in listeners) {
          listener.received(params["streamId"].asString, json)
        }
      }

      val id = json["id"].asString
      val consumer = consumerMap.remove(id)
      consumer?.received(json)
    }

    override fun onClose() {}
    override fun onPing() {}
    override fun onPong() {}
  }


  companion object {
    fun getInstance(project: Project): DartToolingDaemonService = project.service()

    private const val MIN_SDK_VERSION: String = "3.4"
    private const val STARTUP_MESSAGE_PREFIX = "The Dart Tooling Daemon is listening on "
  }
}
