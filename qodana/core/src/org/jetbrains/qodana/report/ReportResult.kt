package org.jetbrains.qodana.report

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts.NotificationContent

sealed interface ReportResult<out ReportT : LoadedSarif, out ErrorT : ReportResult.Error<ErrorT>> {
  class Success<out T : LoadedSarif>(val loadedSarifReport: T) : ReportResult<T, Nothing>

  class Fail<out T : Error<T>> (val error: T) : ReportResult<Nothing, T>

  interface Error<out T : Error<T>> {
    fun throwException(): Nothing

    fun spawnNotification(project: Project?, contentProvider: (T) -> @NotificationContent String)
  }
}