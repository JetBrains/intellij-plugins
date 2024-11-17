package org.jetbrains.qodana.webUi.handlers

import com.intellij.ide.impl.ProjectUtil
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.QueryStringDecoder
import kotlinx.coroutines.withContext
import org.jetbrains.ide.RestService
import org.jetbrains.io.send
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.stats.SetupCiDialogSource
import org.jetbrains.qodana.ui.ci.showSetupCIDialogOrWizardWithYaml


/**
 * @api {post} qodana/setupCi Open IDE window and Qodana "Setup CI" dialog in it, used at the end of Qodana web UI onboarding (Complete button)
 * Works only if valid _qdt qodana token is present in request url parameters or referrer's, otherwise 404
 *
 * @apiGroup Qodana
 *
 * @apiExample {curl} Example
 * curl http://localhost:63342/api/qodana/setupCi
 *
 * See [org.jetbrains.qodana.webUi.handlers.QodanaWebUiSetupCiHandlerTest] for examples
 */
class QodanaWebUiSetupCiHandler : RestService() {
  override fun getServiceName(): String = "$QODANA_WEB_UI_HANDLER_PATH/setupCi"

  override fun isMethodSupported(method: HttpMethod): Boolean = method == HttpMethod.POST

  override fun execute(urlDecoder: QueryStringDecoder, request: FullHttpRequest, context: ChannelHandlerContext): String? {
    processQodanaWebUiRequestAsync(request, context) { project, _ ->
      withContext(QodanaDispatchers.Ui) {
        ProjectUtil.focusProjectWindow(project, stealFocusIfAppInactive = true)
      }
      showSetupCIDialogOrWizardWithYaml(project, SetupCiDialogSource.LOCAL_REPORT).join()

      HttpResponseStatus.OK.send(context.channel(), request)
      true
    }
    return null
  }
}