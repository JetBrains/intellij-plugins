// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.toolingDaemon

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.KillableProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.EventDispatcher
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread
import com.intellij.util.concurrency.annotations.RequiresReadLock
import com.intellij.util.io.BaseOutputReader
import com.intellij.util.io.URLUtil
import com.jetbrains.lang.dart.ide.devtools.DartDevToolsService
import com.jetbrains.lang.dart.sdk.DartSdk
import com.jetbrains.lang.dart.sdk.DartSdkLibUtil
import com.jetbrains.lang.dart.sdk.DartSdkUtil
import de.roderick.weberknecht.WebSocket
import de.roderick.weberknecht.WebSocketEventHandler
import de.roderick.weberknecht.WebSocketException
import de.roderick.weberknecht.WebSocketMessage
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicInteger

@Service(Service.Level.PROJECT)
class DartToolingDaemonService private constructor(private val project: Project) : Disposable {

  private lateinit var dtdProcessHandler: KillableProcessHandler
  private var serviceRunning = false

  private lateinit var webSocket: WebSocket
  var webSocketReady = false
    private set
  var uri: String? = null

  private var secret: String? = null

  private var lastSentRootUris: List<String> = emptyList()

  private val nextRequestId = AtomicInteger()
  private val consumerMap: MutableMap<String, DartToolingDaemonConsumer> = mutableMapOf()

  private val eventDispatcher: EventDispatcher<DartToolingDaemonListener> = EventDispatcher.create(DartToolingDaemonListener::class.java)

  @Throws(ExecutionException::class)
  fun startService() {
    if (serviceRunning) {
      logger.error("Ignoring startService() call. Dart Tooling Daemon is already running.")
      return
    }

    val sdk = DartSdk.getDartSdk(project)?.takeIf { isDartSdkVersionSufficient(it) } ?: return

    val commandLine = GeneralCommandLine().withWorkDirectory(sdk.homePath)
    commandLine.exePath = FileUtil.toSystemDependentName(DartSdkUtil.getDartExePath(sdk))
    commandLine.charset = StandardCharsets.UTF_8
    commandLine.addParameter("tooling-daemon")
    commandLine.addParameter("--machine")

    logger.info("Starting Dart Tooling Daemon, sdk ${sdk.version}")
    dtdProcessHandler = object : KillableProcessHandler(commandLine) {
      override fun readerOptions(): BaseOutputReader.Options = BaseOutputReader.Options.forMostlySilentProcess()
    }
    dtdProcessHandler.addProcessListener(DtdProcessListener())
    dtdProcessHandler.startNotify()
  }

  private fun onServiceStarted(uri: String?, secret: String?) {
    serviceRunning = true
    this.secret = secret
    this.uri = uri
    DartDevToolsService.getInstance(project).startService(uri)
    uri?.let { connectToDtdWebSocket(it) }
  }

  private fun connectToDtdWebSocket(uri: String) {
    try {
      webSocket = WebSocket(URI(uri))
      webSocket.eventHandler = DtdWebSocketEventHandler()
      webSocket.connect()
    }
    catch (e: Exception) {
      logger.error("Failed to connect to Dart Tooling Daemon, uri: $uri", e)
    }
  }

  @Throws(WebSocketException::class)
  fun sendRequest(method: String, params: JsonObject, includeSecret: Boolean, consumer: DartToolingDaemonConsumer) {
    if (!webSocketReady) {
      logger.warn("sendRequest(\"$method\") called when the socket is not ready")
      return
    }

    val request = JsonObject()
    request.addProperty("jsonrpc", "2.0")
    request.addProperty("method", method)

    val id = nextRequestId.incrementAndGet().toString()
    request.addProperty("id", id)
    secret?.takeIf { includeSecret }?.let { params.addProperty("secret", it) }
    request.add("params", params)

    consumerMap[id] = consumer

    val requestString = request.toString()
    logger.debug("--> $requestString")
    webSocket.send(requestString)
  }

  fun addToolingDaemonListener(listener: DartToolingDaemonListener, parentDisposable: Disposable) =
    eventDispatcher.addListener(listener, parentDisposable)

  fun ensureRootsUpToDate() {
    if (!webSocketReady) return

    ReadAction
      .nonBlocking(Callable { calcRootUris() })
      .coalesceBy(this)
      .expireWith(this)
      .finishOnUiThread(ModalityState.nonModal()) { rootUris: List<String> ->
        if (!webSocketReady) return@finishOnUiThread
        if (lastSentRootUris == rootUris) return@finishOnUiThread

        // https://pub.dev/documentation/dtd/latest/dtd/DTDConnection/setIDEWorkspaceRoots.html
        val params = JsonObject()
        val rootUrisArray = JsonArray()
        rootUris.forEach { rootUrisArray.add(it) }
        params.add("roots", rootUrisArray)
        sendRequest("FileSystem.setIDEWorkspaceRoots", params, true) {}

        lastSentRootUris = rootUris
      }
      .submit(AppExecutorUtil.getAppExecutorService())
  }

  @RequiresReadLock
  @RequiresBackgroundThread
  // A simplified version of com.jetbrains.lang.dart.analyzer.DartServerRootsHandler.calcIncludedAndExcludedDartRoots
  private fun calcRootUris(): List<String> {
    DartSdk.getDartSdk(project) ?: return emptyList()

    val newIncludedRoots: MutableList<String> = mutableListOf()
    for (module in DartSdkLibUtil.getModulesWithDartSdkEnabled(project)) {
      for (contentEntry in ModuleRootManager.getInstance(module).contentEntries) {
        val contentEntryUrl = contentEntry.url
        if (contentEntryUrl.startsWith(URLUtil.FILE_PROTOCOL + URLUtil.SCHEME_SEPARATOR)) {
          newIncludedRoots.add(contentEntryUrl)
        }
      }
    }

    return newIncludedRoots
  }

  override fun dispose() {
    if (::dtdProcessHandler.isInitialized && !dtdProcessHandler.isProcessTerminated) {
      dtdProcessHandler.killProcess()
    }
  }

  private fun isDartSdkVersionSufficient(sdk: DartSdk): Boolean =
    StringUtil.compareVersionNumbers(sdk.version, MIN_SDK_VERSION) >= 0


  private inner class DtdProcessListener : ProcessListener {
    override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
      logger.debug("DTD output: ${event.text}")
      if (serviceRunning) return

      // The first line of text is the command issued, which can be ignored.
      val text = event.text.trim().takeUnless { it.endsWith(" tooling-daemon --machine") }
                 ?: return

      var uri: String? = null
      var secret: String? = null
      try {
        val json = JsonParser.parseString(text) as JsonObject
        val details = json["tooling_daemon_details"].asJsonObject
        uri = details["uri"].asString
        secret = details["trusted_client_secret"].asString
      }
      catch (e: Exception) {
        logger.warn("Failed to parse DTD init message. Error: ${e.message}. DTD message: $text")
      }
      finally {
        onServiceStarted(uri, secret)
      }
    }

    override fun processTerminated(event: ProcessEvent) {
      serviceRunning = false
      webSocketReady = false
      logger.info("DTD terminated, exit code: ${event.exitCode}")
    }
  }

  private inner class DtdWebSocketEventHandler : WebSocketEventHandler {
    override fun onOpen() {
      logger.info("Connected to DTD successfully")
      webSocketReady = true
      ensureRootsUpToDate()

      // Fake request to make sure the tooling daemon works
      //val params = JsonObject()
      //params.addProperty("streamId", "foo_stream")
      //sendRequest("streamListen", params) { response ->
      //  println("received response from streamListen")
      //  println(response)
      //}
    }

    override fun onMessage(message: WebSocketMessage) {
      val text = message.text
      logger.debug("<-- $text")

      val json: JsonObject = try {
        JsonParser.parseString(text) as JsonObject
      }
      catch (e: Exception) {
        logger.warn("Failed to parse message, error: ${e.message}, message: $text")
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

    override fun onPing() {}
    override fun onPong() {}

    override fun onClose() {
      webSocketReady = false
      secret = null
      lastSentRootUris = emptyList()
    }
  }


  companion object {
    @JvmStatic
    fun getInstance(project: Project): DartToolingDaemonService = project.service()

    private const val MIN_SDK_VERSION: String = "3.4"
    private val logger = logger<DartToolingDaemonService>()
  }
}
