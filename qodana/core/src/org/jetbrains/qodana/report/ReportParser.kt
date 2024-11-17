package org.jetbrains.qodana.report

import com.google.gson.JsonParseException
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts
import com.jetbrains.qodana.sarif.SarifUtil
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.notifications.QodanaNotifications
import java.nio.file.Path
import kotlin.io.path.isRegularFile

object ReportParser {
  fun parseReport(path: Path): ReportResult<NotValidatedSarif, ParserError> {
    if (!path.isRegularFile()) return ReportResult.Fail(FileNotExists)
    try {
      return ReportResult.Success(NotValidatedSarif(SarifUtil.readReport(path)))
    }
    catch (e: JsonParseException) {
      return ReportResult.Fail(JsonParseFailed(e))
    }
  }

  sealed class ParserError: ReportResult.Error<ParserError> {
    override fun throwException(): Nothing = error("Error during report parsing, $this")

    override fun spawnNotification(project: Project?, contentProvider: (ParserError) -> @NlsContexts.NotificationContent String) {
      QodanaNotifications.General.notification(
        QodanaBundle.message("notification.title.cant.parse.report"),
        contentProvider.invoke(this),
        NotificationType.WARNING
      ).notify(project)
    }
  }

  object FileNotExists : ParserError()

  internal class JsonParseFailed(private val e: JsonParseException) : ParserError() {
    val message: String? = e.cause?.localizedMessage ?: e.localizedMessage

    override fun throwException(): Nothing {
      throw e
    }
  }
}