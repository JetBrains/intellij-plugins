package com.jetbrains.cidr.cpp.embedded.platformio.home

import com.google.gson.JsonObject
import com.intellij.ide.RecentProjectsManagerBase
import com.intellij.util.asSafely
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle
import com.jetbrains.cidr.cpp.embedded.platformio.project.LOG
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler
import org.eclipse.lsp4j.jsonrpc.messages.Message
import org.eclipse.lsp4j.jsonrpc.messages.RequestMessage
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage
import org.java_websocket.client.WebSocketClient
import org.java_websocket.framing.CloseFrame
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.util.concurrent.atomic.AtomicInteger

class PlatformioWebSocketClient(port: Int,
                                private val projectOpener: (String?) -> Unit,
                                private val documentOpener: (String?) -> Unit,
                                private val diagnostics: (String?) -> Unit) : WebSocketClient(
  URI("ws://127.0.0.1:$port/wsrpc")) {
  private val jsonRpc = MessageJsonHandler(emptyMap())

  private val rpcIds = AtomicInteger(2)
  private fun Message.send() {
    val msg = jsonRpc.serialize(this)
    LOG.debug("=>$msg")
    send(msg)
  }

  private fun jsonRpcRequest(method: String,
                             params: List<Any?> = emptyList(),
                             id: String = rpcIds.incrementAndGet().toString()): RequestMessage {
    return RequestMessage().apply {
      this.method = method
      this.params = params
      this.id = id
    }
  }

  private val CORE_VERSION_REQ_ID = "13241234"
  override fun onOpen(handshakedata: ServerHandshake?) {
    jsonRpcRequest(method = "core.version", id = CORE_VERSION_REQ_ID).send()
  }

  private fun ResponseMessage.resultField(name: String): String? =
    this.result.asSafely<JsonObject>()?.get(name)?.asString

  /* Communication example
  =>{"jsonrpc":"2.0","id":"0","method":"core.version","params":[]}
  <={"jsonrpc": "2.0", "id": "13241234", "result": "6.1.7b1"}
  =>{"jsonrpc":"2.0","id":"3","method":"ide.listen_commands","params":[]}
  <={"jsonrpc": "2.0", "id": "3", "result": {"id": "ide-get_pio_project_dirs-1682007518.323943", "method": "get_pio_project_dirs", "params": null}}
  =>{"jsonrpc":"2.0","id":"4","method":"ide.on_command_result","params":["ide-get_pio_project_dirs-1682007518.323943",["D:/work/tests/1","D:/work/tests/2"]]}
  <={"jsonrpc": "2.0", "id": "4", "result": true}
  =>{"jsonrpc":"2.0","id":"5","method":"ide.listen_commands","params":[]}
  <={"jsonrpc": "2.0", "id": "5", "result": {"id": "ide-open_project-1682007518.3249447", "method": "open_project", "params": "C:\\Users\\smith\\Documents\\PlatformIO\\Projects\\jjjjjjj"}}
  <={"jsonrpc": "2.0", "id": "6", "result": true}
  */

  override fun onMessage(messageText: String?) {
    LOG.debug("<=$messageText")
    if (messageText != null) {
      when (val message = jsonRpc.parseMessage(messageText)) {
        is ResponseMessage -> {
          val methodName = message.resultField("method")
          val nestedId = message.resultField("id")
          fun pioSendResult(result: Any) = jsonRpcRequest(method = "ide.on_command_result", params = listOf(nestedId, result)).send()

          when {
            message.error != null -> {
              val errorMsg = "jsonRPC error(${message.error.code}): ${message.error.message})"
              diagnostics.invoke(errorMsg)
              LOG.error(errorMsg)
            }

            message.id == CORE_VERSION_REQ_ID -> jsonRpcRequest("ide.listen_commands").send()

            methodName == "get_pio_project_dirs" -> {
              pioSendResult(RecentProjectsManagerBase.getInstanceEx().getRecentPaths())
            }

            methodName == "open_project" -> {
              pioSendResult(true)
              projectOpener.invoke(message.resultField("params"))
            }
            methodName == "open_text_document" -> {
              pioSendResult(true)
              val docPath = message.result.asSafely<JsonObject>()?.get("params")?.asJsonObject?.get("path")?.asString
              documentOpener.invoke(docPath)
            }

            methodName == null -> jsonRpcRequest("ide.listen_commands").send()
            else -> {
              val msg = "Unexpected pio rpc method call: $messageText"
              diagnostics.invoke(msg)
              LOG.error(msg)
            }
          }
        }
        else -> {
          val msg = "Unexpected message: $messageText"
          diagnostics.invoke(msg)
          LOG.error(msg)
        }
      }
    }
  }

  override fun onError(ex: Exception) {
    diagnostics(ClionEmbeddedPlatformioBundle.message("platformio.websocket.error", ex.message))
    LOG.error(ex)
  }

  override fun onClose(code: Int, reason: String, remote: Boolean) {
    if (remote || code != CloseFrame.NORMAL) {
      LOG.error("PlatformIO websocket closed($code): $reason")
    }
    else {
      LOG.debug("PlatformIO websocket closed($code): $reason")
    }
  }

}