// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.toolingDaemon

import com.google.common.collect.Maps
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.intellij.execution.ExecutionException
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
import de.roderick.weberknecht.WebSocket
import de.roderick.weberknecht.WebSocketEventHandler
import de.roderick.weberknecht.WebSocketException
import de.roderick.weberknecht.WebSocketMessage
import java.net.URI
import java.net.URISyntaxException
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicInteger

@Service(Service.Level.PROJECT)
class DartToolingDaemonService(private val myProject: Project) : Disposable {

  private val MIN_SDK_VERSION: String = "3.4"

  private val STARTUP_MESSAGE_PREFIX = "The Dart Tooling Daemon is listening on "

  private var myWebSocket: WebSocket? = null
  private val nextRequestId = AtomicInteger()

  private val consumerMap: MutableMap<String, ToolingDaemonConsumer> = Maps.newHashMap()

  private val listeners: MutableList<ToolingDaemonListener> = ArrayList()

  @Throws(ExecutionException::class)
  fun startService() {
    val sdk = DartSdk.getDartSdk(myProject)
    if (sdk == null || !isDartSdkVersionSufficient(sdk)) {
      return
    }
    val result = GeneralCommandLine().withWorkDirectory(sdk.homePath)
    result.charset = StandardCharsets.UTF_8
    result.exePath = FileUtil.toSystemDependentName("dart")
    result.addParameter("tooling-daemon")

    val handler = ColoredProcessHandler(result)
    handler.addProcessListener(object : ProcessListener {
      override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
        val text = event.text.trim { it <= ' ' }
        if (text.startsWith(STARTUP_MESSAGE_PREFIX)) {
          val address: String = text.split(STARTUP_MESSAGE_PREFIX.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().get(1)
          try {
            myWebSocket = WebSocket(URI("ws://$address/ws"))
            myWebSocket!!.eventHandler = object : WebSocketEventHandler {
              override fun onClose() {
              }

              override fun onMessage(message: WebSocketMessage) {
                val json: JsonObject
                try {
                  json = JsonParser.parseString(message.text) as JsonObject
                }
                catch (e: Exception) {
                  println("Parse message failed: " + message.text + e)
                  return
                }

                val method = json["method"]?.asString
                if (method == "streamNotify") {
                  val params = json["params"].asJsonObject
                  for (listener in java.util.ArrayList(listeners)) {
                    listener.received(params["streamId"].asString, json)
                  }
                }

                val id = json["id"].asString
                val consumer = consumerMap.remove(id)
                consumer?.received(json)
              }

              override fun onOpen() {
                // Fake request to make sure the tooling daemon works
                val params = JsonObject()
                params.addProperty("streamId", "foo_stream")
                try {
                  sendRequest("streamListen", params,
                              ToolingDaemonConsumer { response ->
                                println("received response from streamListen")
                                println(response)
                              })
                }
                catch (e: WebSocketException) {
                  throw RuntimeException(e)
                }
              }

              override fun onPing() {
              }

              override fun onPong() {
              }
            }
            myWebSocket!!.connect()
          }
          catch (e: WebSocketException) {
            throw RuntimeException(e)
          }
          catch (e: URISyntaxException) {
            throw RuntimeException(e)
          }
        }
      }
    })

    handler.startNotify()
  }

  @Throws(WebSocketException::class)
  fun sendRequest(method: String?, params: JsonObject?, consumer: ToolingDaemonConsumer?) {
    val request = JsonObject()
    request.addProperty("jsonrpc", "2.0")
    request.addProperty("method", method)

    val id = nextRequestId.incrementAndGet().toString()
    request.addProperty("id", id)
    request.add("params", params)

    if (consumer != null) {
      consumerMap.put(id, consumer)
    }
    myWebSocket!!.send(request.toString())
  }

  fun addToolingDaemonListener(listener: ToolingDaemonListener) {
    listeners.add(listener)
  }

  fun removeToolingDaemonListener(listener: ToolingDaemonListener?) {
    listeners.remove(listener)
  }

  override fun dispose() {
  }

  private fun isDartSdkVersionSufficient(sdk: DartSdk): Boolean {
    return StringUtil.compareVersionNumbers(sdk.version, MIN_SDK_VERSION) >= 0
  }

  companion object {
    fun getInstance(project: Project): DartToolingDaemonService {
      return project.getService(DartToolingDaemonService::class.java)
    }
  }

}