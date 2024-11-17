package org.jetbrains.qodana.cloud.authorization

import com.google.gson.GsonBuilder
import com.intellij.util.Urls
import com.intellij.util.io.origin
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.*
import kotlinx.coroutines.launch
import org.jetbrains.ide.RestService
import org.jetbrains.qodana.cloud.QodanaCloudStateService
import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.cloud.currentQodanaCloudFrontendUrl
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaApplicationScope
import org.jetbrains.qodana.registry.QodanaRegistry


/**
 * @api {post} qodana/license-agreement pass to the IDE message that user has accepted the license agreement (called by qodana.cloud)
 * finishes the authorization in IDE
 * @apiGroup Qodana
 */
class QodanaLicenseAgreementHandler : RestService() {
  override fun getServiceName(): String = "$QODANA_HANDLER_PREFIX/license-agreement"

  override fun isMethodSupported(method: HttpMethod): Boolean {
    return method == HttpMethod.POST
  }

  override fun isOriginAllowed(request: HttpRequest): OriginCheckResult {
    return if (request.origin?.let { Urls.newFromEncoded(it) } == currentQodanaCloudFrontendUrl()) OriginCheckResult.ALLOW else OriginCheckResult.FORBID
  }

  override fun execute(urlDecoder: QueryStringDecoder, request: FullHttpRequest, context: ChannelHandlerContext): String? {
    if (!QodanaRegistry.isQodanaLicenseAgreementCallbackEnabled) {
      sendStatus(HttpResponseStatus.NOT_FOUND, false, context.channel())
      return null
    }

    val userState = QodanaCloudStateService.getInstance().userState.value
    when(userState) {
      is UserState.Authorized -> {
        sendOk(request, context)
      }
      is UserState.Authorizing -> {
        val gson = GsonBuilder().create()
        val response = gson.fromJson<LicenseAcceptedResponse>(createJsonReader(request), LicenseAcceptedResponse::class.java)
        qodanaApplicationScope.launch(QodanaDispatchers.Default) {
          userState.licenseAgreementAcceptedCallback(response.userId, response.accepted)
        }

        sendOk(request, context)
      }
      is UserState.NotAuthorized -> {
        sendStatus(HttpResponseStatus.NOT_FOUND, false, context.channel())
      }
    }
    return null
  }
}

private class LicenseAcceptedResponse(
  val userId: String,
  val accepted: Boolean
)