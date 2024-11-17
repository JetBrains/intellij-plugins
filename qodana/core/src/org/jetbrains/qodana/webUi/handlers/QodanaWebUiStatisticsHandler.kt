package org.jetbrains.qodana.webUi.handlers

import com.intellij.internal.statistic.utils.StatisticsUploadAssistant
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.util.io.BufferExposingByteArrayOutputStream
import com.intellij.util.PlatformUtils
import com.intellij.util.application
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.QueryStringDecoder
import org.jetbrains.ide.RestService

/**
 * @api {get} qodana/statistics Is sending and collecting statistics enabled, used by qodana frontend
 * Works only if valid _qdt qodana token is present in request url parameters or referrer's, otherwise 404
 *
 * @apiGroup Qodana
 *
 * @apiSuccess {Boolean} enabled
 *
 * @apiExample {curl} Query parameters
 * curl http://localhost:63342/api/qodana/statistics
 *
 * See [org.jetbrains.qodana.webUi.handlers.QodanaWebUiStatisticsHandlerTest] for examples
 */
class QodanaWebUiStatisticsHandler : RestService() {
  override fun getServiceName(): String = "$QODANA_WEB_UI_HANDLER_PATH/statistics"

  override fun execute(urlDecoder: QueryStringDecoder, request: FullHttpRequest, context: ChannelHandlerContext): String? {
    processQodanaWebUiRequestAsync(request, context) { _, _ ->
      val out = BufferExposingByteArrayOutputStream()
      val writer = createJsonWriter(out)

      writer.beginObject()
      writer.name("enabled").value(areStatisticsEnabled())
      writer.endObject()

      writer.close()
      send(out, request, context)
      true
    }
    return null
  }

  private fun areStatisticsEnabled(): Boolean {
    return !application.isUnitTestMode &&
           StatisticsUploadAssistant.isCollectAllowed() &&
           (ApplicationInfo.getInstance() == null || PlatformUtils.isJetBrainsProduct())
  }
}