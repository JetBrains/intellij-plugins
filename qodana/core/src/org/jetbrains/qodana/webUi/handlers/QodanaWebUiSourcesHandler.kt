package org.jetbrains.qodana.webUi.handlers

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.EmptyHttpHeaders
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.QueryStringDecoder
import kotlinx.coroutines.withContext
import org.jetbrains.ide.HttpRequestHandler
import org.jetbrains.qodana.QodanaWebUiSources
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.registry.QodanaRegistry
import java.io.FileInputStream
import java.io.InputStream
import java.net.URI
import kotlin.io.path.Path

const val QODANA_WEB_UI_SOURCES_HANDLER_NAME = "qodana.ide"
private const val PREFIX = "/$QODANA_WEB_UI_SOURCES_HANDLER_NAME"

/**
 * @api Qodana web ui static source file (html, css, js, etc.)
 * Works only if valid _qdt qodana token is present in request url parameters or referrer's, otherwise 404
 *
 * @apiGroup Qodana
 *
 * @apiExample {curl}
 * curl http://localhost:63342/qodana.ide/index.html
 *
 * * See [org.jetbrains.qodana.webUi.handlers.QodanaWebUiSourcesHandlerTest] for examples
 */
class QodanaWebUiSourcesHandler : HttpRequestHandler() {
  override fun isSupported(request: FullHttpRequest): Boolean {
    return super.isSupported(request) && request.uri().startsWith(PREFIX)
  }

  override fun process(urlDecoder: QueryStringDecoder, request: FullHttpRequest, context: ChannelHandlerContext): Boolean {
    processQodanaWebUiRequestAsync(request, context, verifySameOrigin = false) { _, _, ->
      val requestedSourceFile = getRequestedSourceFile(request) ?: return@processQodanaWebUiRequestAsync false
      withContext(QodanaDispatchers.IO) {
        val sourceInputStream = getSourceInputStream(requestedSourceFile) ?: return@withContext false
        val data = sourceInputStream.use {
          it.readAllBytes()
        }
        sendData(data, requestedSourceFile, request, context.channel(), EmptyHttpHeaders.INSTANCE)
        true
      }
    }
    return true
  }

  private fun getRequestedSourceFile(request: FullHttpRequest): String? {
    val uri = URI(request.uri()).path
    if (!uri.startsWith(PREFIX)) return null

    val sourceFile = uri.substring(PREFIX.length).trimStart('/')
    if (sourceFile.isEmpty()) return null
    if (".." in sourceFile) return null

    return sourceFile
  }

  private fun getSourceInputStream(sourceFile: String): InputStream? {
    val sourcesPathInRegistry = QodanaRegistry.webUiSourcesPath
    if (sourcesPathInRegistry.isNotEmpty()) {
      return FileInputStream(Path(sourcesPathInRegistry, sourceFile).toFile())
    }
    return QodanaWebUiSources.getSource(sourceFile)
  }
}