// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.analyzer

import com.google.dart.server.AnalysisServerSocket
import com.google.dart.server.DartLspWorkspaceApplyEditRequestConsumer
import com.google.dart.server.ShowMessageRequestConsumer
import com.google.dart.server.internal.remote.RemoteAnalysisServerImpl
import com.intellij.ide.BrowserUtil
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import org.dartlang.analysis.server.protocol.DartLspApplyWorkspaceEditParams
import org.dartlang.analysis.server.protocol.DartLspApplyWorkspaceEditResult
import org.dartlang.analysis.server.protocol.MessageAction
import org.dartlang.analysis.server.protocol.MessageType

internal class DartAnalysisServerImpl(private val project: Project, socket: AnalysisServerSocket) : RemoteAnalysisServerImpl(socket) {

  override fun server_openUrlRequest(url: String) = BrowserUtil.browse(url)

  override fun server_showMessageRequest(
    messageType: String,
    message: @NlsSafe String,
    messageActions: List<MessageAction>,
    consumer: ShowMessageRequestConsumer,
  ) {
    val notificationType: NotificationType = when (messageType) {
      MessageType.ERROR -> NotificationType.ERROR
      MessageType.WARNING -> NotificationType.WARNING
      else -> NotificationType.INFORMATION
    }

    NotificationGroupManager.getInstance()
      .getNotificationGroup("Dart Analysis Server")
      .createNotification(message, notificationType)
      .also { notification ->
        for (messageAction in messageActions) {
          val actionLabel: @NlsSafe String = messageAction.label
          notification.addAction(object : AnAction(actionLabel) {
            override fun actionPerformed(e: AnActionEvent) {
              notification.expire()
              consumer.computedMessageActions(actionLabel)
            }
          })
        }
      }
      .notify(project)
  }

  override fun lsp_workspaceApplyEdit(params: DartLspApplyWorkspaceEditParams, consumer: DartLspWorkspaceApplyEditRequestConsumer) {
    consumer.workspaceEditApplied(DartLspApplyWorkspaceEditResult(false))
  }
}
