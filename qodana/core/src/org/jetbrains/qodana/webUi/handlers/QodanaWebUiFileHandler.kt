package org.jetbrains.qodana.webUi.handlers

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import io.netty.buffer.ByteBufInputStream
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.QueryStringDecoder
import kotlinx.coroutines.runInterruptible
import org.jetbrains.ide.RestService
import org.jetbrains.io.send
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.notifications.QodanaNotifications
import org.jetbrains.qodana.refreshVcsFileStatus
import org.jetbrains.qodana.staticAnalysis.inspections.config.QODANA_YAML_CONFIG_FILENAME
import org.jetbrains.qodana.staticAnalysis.inspections.config.QODANA_YML_CONFIG_FILENAME
import org.jetbrains.qodana.staticAnalysis.inspections.runner.FULL_SARIF_REPORT_NAME
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * @api {post} qodana/file Save qodana.yaml or qodana.sarif.json baseline from qodana web ui, file content – request body
 * Used at the end of Qodana web UI onboarding (Complete button)
 * Works only if valid _qdt qodana token is present in request url parameters or referrer's, otherwise 404
 *
 * @apiGroup Qodana
 *
 * @apiParam {String} path Relative path in project to file where to save, supports only qodana.yaml and qodana.sarif.json
 *
 * @apiExample {curl} Query parameters
 * curl http://localhost:63342/api/qodana/file?path=qodana.yaml&_qdt=token
 *
 * See [org.jetbrains.qodana.webUi.handlers.QodanaWebUiFileHandlerTest] for examples
 */
class QodanaWebUiFileHandler : RestService() {
  override fun getServiceName(): String = "$QODANA_WEB_UI_HANDLER_PATH/file"

  override fun isMethodSupported(method: HttpMethod): Boolean = method == HttpMethod.POST

  override fun execute(urlDecoder: QueryStringDecoder, request: FullHttpRequest, context: ChannelHandlerContext): String? {
    processQodanaWebUiRequestAsync(request, context) { project, _ ->
      val channel = context.channel()
      if (!isValidContentType(request)) {
        HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE.send(channel, request)
        return@processQodanaWebUiRequestAsync true
      }

      val path = urlDecoder.parameters()["path"]?.firstOrNull() ?: return@processQodanaWebUiRequestAsync false
      val success = when (path) {
        "qodana.yaml" -> {
          createOrRewriteFile(
            project,
            request,
            possibleNames = listOf(QODANA_YAML_CONFIG_FILENAME, QODANA_YML_CONFIG_FILENAME),
            fileNameToCreate = QODANA_YAML_CONFIG_FILENAME,
            notificationProvider = { isNewFile, file ->
              notificationSavedQodanaYaml(project, isNewFile, file)
            }
          )
        }
        "qodana.sarif.json" -> {
          createOrRewriteFile(
            project,
            request,
            possibleNames = listOf(FULL_SARIF_REPORT_NAME),
            fileNameToCreate = FULL_SARIF_REPORT_NAME,
            notificationProvider = ::notificationSavedSarifBaseline
          )
        }
        else -> false
      }
      if (success) {
        HttpResponseStatus.OK.send(channel, request)
      }
      success
    }
    return null
  }

  private fun isValidContentType(request: FullHttpRequest): Boolean {
    return request.headers()["Content-Type"] != "application/octet-stream"
  }

  private suspend fun createOrRewriteFile(
    project: Project,
    request: FullHttpRequest,
    possibleNames: List<String>,
    fileNameToCreate: String,
    notificationProvider: (isNewFile: Boolean, file: VirtualFile) -> Notification
  ): Boolean {
    val projectVirtualFile = project.guessProjectDir() ?: return false
    val alreadyPresentFile = possibleNames
      .asSequence()
      .mapNotNull { projectVirtualFile.findChild(it) }
      .firstOrNull()

    if (alreadyPresentFile != null) {
      writeToVirtualFile(alreadyPresentFile, request)
      notificationProvider.invoke(false, alreadyPresentFile).notify(project)
      return true
    }
    val newVirtualFile = writeAction {
      projectVirtualFile.createChildData(this, fileNameToCreate)
    }
    writeToVirtualFile(newVirtualFile, request)
    refreshVcsFileStatus(project, newVirtualFile)
    notificationProvider.invoke(true, newVirtualFile).notify(project)
    return true
  }

  private suspend fun writeToVirtualFile(virtualFile: VirtualFile, request: FullHttpRequest) {
    runInterruptible(QodanaDispatchers.IO) {
      ByteBufInputStream(request.content()).use { requestContentStream: ByteBufInputStream ->
        val file = virtualFile.toNioPath()
        Files.copy(requestContentStream, file, StandardCopyOption.REPLACE_EXISTING)
      }
    }
  }
}

private fun notificationSavedSarifBaseline(isNew: Boolean, virtualFile: VirtualFile): Notification {
  val title = QodanaBundle.message("qodana.web.ui.write.baseline.notification.title", if (isNew) 1 else 0)
  return QodanaNotifications.General.notification(
    title,
    QodanaBundle.message("qodana.web.ui.write.baseline.notification.text", virtualFile.presentableName),
    NotificationType.INFORMATION,
    withQodanaIcon = true
  )
}

private fun notificationSavedQodanaYaml(project: Project, isNew: Boolean, virtualFile: VirtualFile): Notification {
  val fileName = virtualFile.presentableName
  val title = QodanaBundle.message("qodana.web.ui.write.yaml.notification.title", if (isNew) 1 else 0, fileName)
  return QodanaNotifications.General.notification(
    null,
    title,
    NotificationType.INFORMATION,
    withQodanaIcon = true
  ).addAction(NotificationAction.createSimpleExpiring(QodanaBundle.message("qodana.web.ui.write.yaml.notification.action.text", fileName)) {
    OpenFileDescriptor(project, virtualFile, ).navigateInEditor(project, true)
  })
}