package org.jetbrains.qodana.staticAnalysis.script.scoped

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.jetbrains.qodana.sarif.model.SarifReport
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import org.jetbrains.qodana.problem.SarifProblem
import org.jetbrains.qodana.report.ReportResult
import org.jetbrains.qodana.report.ReportValidator
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

internal suspend fun computeNextScope(project: Project, report: SarifReport): List<ChangedFile> {
  return withContext(StaticAnalysisDispatchers.Default) {
    val validatedSarif = when (val validatedReport = ReportValidator.validateReport(report)) {
      is ReportResult.Fail -> throw QodanaException("Failed to validate output SARIF: ${validatedReport.error}")
      is ReportResult.Success -> validatedReport.loadedSarifReport
    }

    val problems: List<SarifProblem> = SarifProblem.fromReport(project, validatedSarif, project.guessProjectDir()?.path)
    problems
      .asSequence()
      .filter { !it.isInBaseline && it.relativePathToFile.isNotEmpty() }
      .distinctBy { it.relativePathToFile }
      .map { ChangedFile(it.relativePathToFile, emptyList(), emptyList()) }
      .toList()
  }
}

@OptIn(ExperimentalSerializationApi::class)
internal suspend fun writeNextScope(path: Path, affectedFiles: List<ChangedFile>) {
  runInterruptible(StaticAnalysisDispatchers.IO) {
    path.parent.toFile().mkdirs()
    Files.newOutputStream(path, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE).use { os ->
      Json.encodeToStream(ChangedFiles(affectedFiles), os)
    }
  }
}