package org.jetbrains.qodana.webUi.handlers

import com.intellij.openapi.components.serviceIfCreated
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.util.io.isLocalOrigin
import com.intellij.util.io.referrer
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.QueryStringDecoder
import io.netty.util.ReferenceCountUtil
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.io.send
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.webUi.ActiveWebUi
import org.jetbrains.qodana.webUi.QodanaWebUiService

internal const val QODANA_WEB_UI_HANDLER_PATH = "qodana"
internal const val QODANA_WEB_UI_TOKEN_PARAM = "_qdt"

fun processQodanaWebUiRequestAsync(
  request: FullHttpRequest,
  context: ChannelHandlerContext,
  verifySameOrigin: Boolean = true,
  requestProcessor: suspend (project: Project, activeWebUi: ActiveWebUi) -> Boolean
) {
  val channel = context.channel()
  if (!request.isLocalOrigin()) {
    HttpResponseStatus.NOT_FOUND.send(channel, request)
    return
  }
  if (verifySameOrigin && request.headers()["Sec-Fetch-Site"] != "same-origin") {
    HttpResponseStatus.NOT_FOUND.send(channel, request)
    return
  }
  val token = request.qodanaTokenFromRequest
  if (token == null) {
    HttpResponseStatus.NOT_FOUND.send(channel, request)
    return
  }

  val projects = ProjectManager.getInstance().openProjects.filter { !it.isDisposed }
  val projectWithActiveWebUi = projects.asSequence()
    .mapNotNull { project ->
      val webUiService = project.serviceIfCreated<QodanaWebUiService>() ?: return@mapNotNull null
      val webUiWithMatchingToken = webUiService.activeWebUis.value.firstOrNull { it.token == token } ?: return@mapNotNull null
      project to webUiWithMatchingToken
    }
    .firstOrNull()
  if (projectWithActiveWebUi == null) {
    HttpResponseStatus.NOT_FOUND.send(channel, request)
    return
  }
  val (project, activeWebUi) = projectWithActiveWebUi

  ReferenceCountUtil.retain(request)
  project.qodanaProjectScope.launch(QodanaDispatchers.Default) {
    val nettyChannelClosed = nettyChannelClosedFlow(channel)
    val requestProcessingFinished = suspend {
      var succeeded = false
      try {
        succeeded = requestProcessor.invoke(project, activeWebUi)
      }
      finally {
        ReferenceCountUtil.release(request)
        if (!succeeded) {
          HttpResponseStatus.NOT_FOUND.send(channel, request)
        }
      }
    }.asFlow()
    val webUiClosed = QodanaWebUiService.getInstance(project).activeWebUis.filter { activeWebUis ->
      activeWebUi !in activeWebUis
    }

    merge(nettyChannelClosed, requestProcessingFinished, webUiClosed).first()
  }
}

private val FullHttpRequest.qodanaTokenFromRequest: String?
  get() {
    val requestParameters = QueryStringDecoder(uri()).parameters()
    val referrerParameters = referrer?.let { QueryStringDecoder(it).parameters() }

    return (requestParameters[QODANA_WEB_UI_TOKEN_PARAM] ?: referrerParameters?.get(QODANA_WEB_UI_TOKEN_PARAM))?.lastOrNull()
  }

private suspend fun nettyChannelClosedFlow(channel: Channel): Flow<Unit> {
  return callbackFlow {
    val listener: (Any) -> Unit = {
      trySendBlocking(Unit)
      this.channel.close()
    }
    val closeFuture = channel.closeFuture()
    closeFuture.addListener(listener)
    awaitClose { closeFuture.removeListener(listener) }
  }
}
