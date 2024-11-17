package org.jetbrains.qodana.actions

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.highlight.QODANA_REVISION_DATA
import org.jetbrains.qodana.highlight.QodanaRevisionData
import org.jetbrains.qodana.notifications.QodanaNotifications
import org.jetbrains.qodana.problem.SarifProblem

private const val REVISION_SYMBOLS_COUNT_TO_PRINT = 7

sealed interface RevisionFileOpener {
  class Success(val action: () -> Unit) : RevisionFileOpener

  class Error(@NlsContexts.NotificationContent val message: String) : RevisionFileOpener
}

class OpenProblemWithRevisionAction(private val sarifProblem: SarifProblem) : NotificationAction(
  QodanaBundle.message("notification.action.open.file.with.qodana.report.revision")
) {
  /**
   * Returns [RevisionFileOpener.Success], which is used for opening file with Qodana problem revision,
   * [RevisionFileOpener.Error] if it's not possible to open revision
   */
  private fun getRevisionFileOpener(project: Project): RevisionFileOpener {
    val file = sarifProblem.getVirtualFile(project)
    val document = file?.let { runReadAction { FileDocumentManager.getInstance().getDocument(file) } }

    if (document == null) {
      val fileNotFoundMessage = QodanaBundle.message("notification.content.cant.find.file", sarifProblem.relativePathToFile)
      return RevisionFileOpener.Error(fileNotFoundMessage)
    }

    val revisionData = document.getUserData(QODANA_REVISION_DATA)
    if (revisionData == null || revisionData !is QodanaRevisionData.VCSInfo) {
      val unexpectedFailMessage = ""
      return RevisionFileOpener.Error(unexpectedFailMessage)
    }

    val revisionPsiFile = revisionData.revisionPsiFiles[sarifProblem.revisionId]
    val revisionVirtualFile = revisionPsiFile?.virtualFile
    val revisionDocument = revisionPsiFile?.viewProvider?.document

    if (revisionVirtualFile == null || revisionDocument == null) {
      val noFileInRevisionMessage = QodanaBundle.message(
        "notification.content.cant.find.file.in.revision",
        file.name, sarifProblem.revisionId?.substring(0, REVISION_SYMBOLS_COUNT_TO_PRINT) ?: ""
      )
      return RevisionFileOpener.Error(noFileInRevisionMessage)
    }

    val problemOffset = sarifProblem.getTextRangeInDocument(revisionDocument)?.startOffset
    val revisionFileOpener = {
      invokeLater {
        val fileEditorManager = FileEditorManager.getInstance(project)
        if (problemOffset == null)
          fileEditorManager.openFile(revisionVirtualFile, true)
        else
          fileEditorManager.openTextEditor(OpenFileDescriptor(project, revisionVirtualFile, problemOffset), true)
      }
    }
    return RevisionFileOpener.Success(revisionFileOpener)
  }

  fun canBePerformed(project: Project): Boolean = getRevisionFileOpener(project) is RevisionFileOpener.Success

  override fun actionPerformed(e: AnActionEvent, notification: Notification) {
    val project = e.project ?: return
    notification.expire()
    when (val revisionFileOpener = getRevisionFileOpener(project)) {
      is RevisionFileOpener.Success -> {
        revisionFileOpener.action.invoke()
      }
      is RevisionFileOpener.Error -> {
        QodanaNotifications.General.notification(
          QodanaBundle.message("notification.title.failed.to.open.file.with.report.revision"),
          revisionFileOpener.message,
          NotificationType.WARNING
        ).notify(project)
      }
    }
  }
}