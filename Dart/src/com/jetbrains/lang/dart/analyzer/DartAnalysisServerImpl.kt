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
import com.intellij.openapi.command.writeCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.util.concurrency.annotations.RequiresWriteLock
import com.jetbrains.lang.dart.DartBundle
import kotlinx.coroutines.launch
import org.dartlang.analysis.server.protocol.*

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
    DartAnalysisServerService.getInstance(project).serviceScope.launch {
      val label: @NlsSafe String? = params.label
      val commandName: String = label ?: DartBundle.message("code.changes.by.dart.analysis.server")

      var applied = false
      try {
        writeCommandAction(project, commandName) {
          applyWorkspaceEdit(params.workspaceEdit)
          consumer.workspaceEditApplied(DartLspApplyWorkspaceEditResult(true))
          applied = true
        }
      }
      finally {
        if (!applied) consumer.workspaceEditApplied(DartLspApplyWorkspaceEditResult(false))
      }
    }
  }

  @RequiresWriteLock
  private fun applyWorkspaceEdit(workspaceEdit: DartLspWorkspaceEdit): Boolean {
    val changes = workspaceEdit.changes ?: return false
    changes.entries.forEach { entry ->
      val uri = entry.key
      val virtualFile = getDartFileInfo(project, uri).findFile() ?: return false
      val document = FileDocumentManager.getInstance().getDocument(virtualFile) ?: return false
      if (!applyTextEdits(document, entry.value)) return false
    }
    return true
  }
}
