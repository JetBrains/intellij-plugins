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
import com.intellij.util.EventDispatcher
import com.intellij.util.io.BaseOutputReader
import com.jetbrains.lang.dart.ide.devtools.DartDevToolsService
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

  private val eventDispatcher: EventDispatcher<DartToolingDaemonListener> = EventDispatcher.create(DartToolingDaemonListener::class.java)

  @Throws(ExecutionException::class)
  fun startService() {
    val sdk = DartSdk.getDartSdk(project)?.takeIf { isDartSdkVersionSufficient(it) } ?: return

    val commandLine = GeneralCommandLine().withWorkDirectory(sdk.homePath)
    commandLine.exePath = FileUtil.toSystemDependentName(DartSdkUtil.getDartExePath(sdk))
    commandLine.charset = StandardCharsets.UTF_8
    commandLine.addParameter("tooling-daemon")
    commandLine.addParameter("--machine")

    dtdProcessHandler = object : ColoredProcessHandler(commandLine) {
      override fun readerOptions(): BaseOutputReader.Options = BaseOutputReader.Options.forMostlySilentProcess()
    }

    dtdProcessHandler.addProcessListener(object : ProcessListener {
      override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
        // The first line of text is the command issued, which can be ignored.
        val text = event.text.trim().takeUnless { it.endsWith("dart tooling-daemon --machine") }
                   ?: return

        event.processHandler.removeProcessListener(this)

        try {
          val json = JsonParser.parseString(text) as JsonObject
          val details = json["tooling_daemon_details"].asJsonObject
          val uri = details["uri"].asString
          val secret = details["trusted_client_secret"].asString
          onServiceStarted(uri, secret)
        }
        catch (e: Exception) {
          logger<DartToolingDaemonService>().warn("Failed to parse DTD init message. Error: ${e.message}. DTD message: $text")
          onServiceStarted(null, null)
        }
      }
    })

    dtdProcessHandler.startNotify()
  }

  private fun onServiceStarted(uri: String?, secret: String?) {
    DartDevToolsService.getInstance(project).startService(uri)
    uri?.let { connectToDtdWebSocket(it) }
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

  fun addToolingDaemonListener(listener: DartToolingDaemonListener, parentDisposable: Disposable) =
    eventDispatcher.addListener(listener, parentDisposable)

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
        val streamId = params["streamId"].asString
        eventDispatcher.multicaster.received(streamId, json)
      }
      else {
        val id = json["id"].asString
        val consumer = consumerMap.remove(id)
        consumer?.received(json)
      }
    }

    override fun onClose() {}
    override fun onPing() {}
    override fun onPong() {}
  }


  companion object {
    fun getInstance(project: Project): DartToolingDaemonService = project.service()

    private const val MIN_SDK_VERSION: String = "3.4"
  }
}
