// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service.lsp

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.intellij.lang.typescript.lsp.JSFrameworkLsp4jServer
import com.intellij.lang.typescript.lsp.TypeScriptLspClientCommandExecutor.Companion.tsSendNotificationSuspending
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.platform.lsp.api.Lsp4jClient
import com.intellij.platform.lsp.api.LspClientManager
import com.intellij.platform.lsp.api.LspServerNotificationsHandler
import com.intellij.platform.lsp.api.LspIntegrationProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification
import org.jetbrains.vuejs.lang.typescript.service.VueLSCoroutineScope
import org.jetbrains.vuejs.lang.typescript.service.VueServiceRuntime
import org.jetbrains.vuejs.lang.typescript.service.plugin.VuePluginTypeScriptService
import java.lang.reflect.Type

interface VueHMLsp4jServer : JSFrameworkLsp4jServer {
  @JsonNotification("tsserver/response")
  fun tsserverResponse(params: JsonArray?)
}

/**
 * For Vue Hybrid mode.
 * Custom Lsp4jClient implementation for Vue LSP to handle custom tsserver bridge calls.
 */
internal class VueHybridModeLsp4jClient<P : LspIntegrationProvider>(
  private val project: Project,
  handler: LspServerNotificationsHandler,
  private val lspServerSupportProvider: Class<P>,
  private val runtime: VueServiceRuntime,
) : Lsp4jClient(handler) {

  @JsonNotification("tsserver/request")
  @Suppress("unused")
  fun tsserverRequest(params: JsonArray?) {
    thisLogger().info("Received tsserver/request: $params")

    val (requestId, command, arguments) = VueTsserverRequest.parse(params)
                                          ?: return

    val server = LspClientManager.getInstance(project)
      .getClients(lspServerSupportProvider)
      .singleOrNull()

    if (server == null) {
      thisLogger().warn("No server found for provider $lspServerSupportProvider")
      return
    }

    VueLSCoroutineScope.instance(project).cs.launch {
      val result = getTsserverResponseWithRetries(command, arguments, requestId)
      val responseJson = JsonArray().apply {
        add(
          JsonArray().apply {
            add(requestId)
            add(result)
          }
        )
      }

      server.tsSendNotificationSuspending("tsserver/response") {
        val lsp4jServer = it as VueHMLsp4jServer
        lsp4jServer.tsserverResponse(responseJson)
      }
    }
  }

  /**
   * The server waits for a `tsserver/response` with this requestId.
   * A missing response blocks every later request of the server, for example a diagnostics pull.
   * So a failed call must still produce a response; a null result stands for "no data".
   * A short retry heals a transient failure, for example the tsserver "No Project" error during
   * the tsserver warm-up. The retry window stays small: the server serializes its requests behind
   * this response, and the server falls back to an inferred project on a null result.
   * Throwable, not Exception: in tests a logged error surfaces as an AssertionError.
   */
  private suspend fun getTsserverResponseWithRetries(
    command: String,
    arguments: JsonElement?,
    requestId: Int,
  ): JsonElement? {
    var lastFailure: Throwable? = null
    repeat(MAX_TSSERVER_ATTEMPTS) {
      try {
        return getTsserverResponse(command, arguments)
      }
      catch (e: CancellationException) {
        throw e
      }
      catch (e: Throwable) {
        lastFailure = e
        delay(TSSERVER_RETRY_DELAY)
      }
    }
    thisLogger().warn("tsserver request '$command' (seq=$requestId) failed after $MAX_TSSERVER_ATTEMPTS attempts", lastFailure)
    return null
  }

  private suspend fun getTsserverResponse(
    command: String,
    arguments: JsonElement?,
  ): JsonElement? {
    val pluggableTypeScriptService = VuePluginTypeScriptService.find(project, runtime)

    if (pluggableTypeScriptService == null) {
      thisLogger().warn("No Vue TypeScript plugin service found for runtime $runtime.")
      return null
    }

    return pluggableTypeScriptService.executeTsserverRequest(
      command = command,
      args = arguments,
    )
  }

  private companion object {
    private const val MAX_TSSERVER_ATTEMPTS: Int = 5
    private val TSSERVER_RETRY_DELAY: Duration = 200.milliseconds
  }
}

/**
 * Vue language server sends tsserver/request params as a positional JSON tuple (not a named-field object),
 * so we use a custom [JsonDeserializer] to map array positions to fields.
 *
 * Wire format: `[[requestId: number, command: string, args: object | array]]`
 * LSP4J unwraps the outer array, so [parse] receives the inner tuple: `[requestId, command, args]`.
 */
@JsonAdapter(VueTsserverRequestDeserializer::class)
private data class VueTsserverRequest(
  val requestId: Int,
  val command: String,
  val args: JsonElement?,
) {
  companion object {
    private val gson = Gson()
    private val LOG = logger<VueTsserverRequest>()

    fun parse(params: JsonArray?): VueTsserverRequest? {
      if (params == null) return null
      return try {
        val request = gson.fromJson(params, VueTsserverRequest::class.java)
        if (!request.command.startsWith("_vue:")) {
          LOG.warn("tsserver/request unexpected command (expected _vue: prefix): ${request.command}")
          return null
        }
        LOG.debug("tsserver/request unpacked: seq=${request.requestId}, command=${request.command}, args=${request.args}")
        request
      }
      catch (e: Exception) {
        LOG.warn("tsserver/request: failed to parse params: $params", e)
        return null
      }
    }
  }
}

private class VueTsserverRequestDeserializer : JsonDeserializer<VueTsserverRequest> {
  override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): VueTsserverRequest {
    val array = json.asJsonArray
    return VueTsserverRequest(
      requestId = array[0].asInt,
      command = array[1].asString,
      args = array[2].takeUnless { it.isJsonNull },
    )
  }
}
