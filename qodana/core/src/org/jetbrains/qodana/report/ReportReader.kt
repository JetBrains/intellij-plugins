package org.jetbrains.qodana.report

import com.intellij.openapi.project.Project
import java.nio.file.Path

object ReportReader {
  fun readReport(path: Path): ReportResult<ValidatedSarif, ReaderError> {
    return when(val parsedReport = ReportParser.parseReport(path)) {
      is ReportResult.Fail -> {
        return ReportResult.Fail(FailedParsing(parsedReport.error))
      }
      is ReportResult.Success -> {
        when(val validatedReport = ReportValidator.validateReport(parsedReport.loadedSarifReport.sarif)) {
          is ReportResult.Fail -> ReportResult.Fail(FailedValidation(validatedReport.error))
          is ReportResult.Success -> validatedReport
        }
      }
    }
  }

  sealed interface ReaderError : ReportResult.Error<ReaderError>

  class FailedParsing(val parserError : ReportParser.ParserError) : ReaderError {
    override fun throwException(): Nothing = parserError.throwException()

    override fun spawnNotification(project: Project?, contentProvider: (ReaderError) -> String) {
      parserError.spawnNotification(project) { error ->
        contentProvider.invoke(FailedParsing(error))
      }
    }
  }

  class FailedValidation(val validatorError : ReportValidator.ValidatorError) : ReaderError {
    override fun throwException(): Nothing = validatorError.throwException()

    override fun spawnNotification(project: Project?, contentProvider: (ReaderError) -> String) {
      validatorError.spawnNotification(project) { error ->
        contentProvider.invoke(FailedValidation(error))
      }
    }
  }
}