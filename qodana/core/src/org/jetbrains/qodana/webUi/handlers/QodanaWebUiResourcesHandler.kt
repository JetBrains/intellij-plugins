package org.jetbrains.qodana.webUi.handlers

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.QueryStringDecoder
import org.jetbrains.ide.RestService
import org.jetbrains.io.FileResponses

/**
 * @api {get} qodana/resources Get resource file needed for qodana web ui (results-allProblems.json and etc.)
 * Works only if valid _qdt qodana token is present in request url parameters or referrer's, otherwise 404
 * @apiGroup Qodana
 *
 * @apiParam {String} file Path to requested file of qodana web ui sources
 *
 * @apiExample {curl} Query parameters
 * curl http://localhost:63342/api/qodana/resources?file=results-allProblems.json&_qdt=token
 *
 * See [org.jetbrains.qodana.webUi.handlers.QodanaWebUiResourcesHandlerTest] for examples
 */
class QodanaWebUiResourcesHandler : RestService() {
  override fun getServiceName(): String = "$QODANA_WEB_UI_HANDLER_PATH/resources"

  override fun execute(urlDecoder: QueryStringDecoder, request: FullHttpRequest, context: ChannelHandlerContext): String? {
    processQodanaWebUiRequestAsync(request, context) { _, activeWebUi ->
      val requestedResource = urlDecoder.parameters()["file"]?.firstOrNull() ?: return@processQodanaWebUiRequestAsync false
      val resourcePath = activeWebUi.qodanaConverterResults.getOutputFile(requestedResource) ?: return@processQodanaWebUiRequestAsync false
      FileResponses.sendFile(request, context.channel(), resourcePath)
      true
    }
    return null
  }
}