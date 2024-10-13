package org.jetbrains.qodana.staticAnalysis.script

import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.vfs.toNioPathOrNull
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.coverageData.QodanaCoverageComputationState
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaMessageReporter
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunContext
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.QodanaRunContextFactory
import java.nio.file.Paths
import kotlin.io.path.*

private const val DEFAULT_PATH = "teamcity-changes.txt"
internal const val TEAMCITY_CHANGES_SCRIPT_NAME = "teamcity-changes"

private const val TEAMCITY_CHANGED_FILES_PATH = "path"

class TeamCityChangesScriptFactory : QodanaScriptFactory {
  override val scriptName = TEAMCITY_CHANGES_SCRIPT_NAME

  override fun parseParameters(parameters: String): Map<String, String> =
    if (parameters.isEmpty()) emptyMap() else mapOf(TEAMCITY_CHANGED_FILES_PATH to parameters)

  override fun createScript(
    config: QodanaConfig,
    messageReporter: QodanaMessageReporter,
    contextFactory: QodanaRunContextFactory,
    parameters: UnvalidatedParameters
  ): QodanaScript {
    val path = parameters.optional<String>(TEAMCITY_CHANGED_FILES_PATH) ?: DEFAULT_PATH
    val changes = parseChangesFile(path, config)
    return TeamcityScript(TeamcityRunContextFactory(contextFactory, changes), AnalysisKind.OTHER)
  }

  private fun parseChangesFile(changesPath: String, config: QodanaConfig): List<TeamcityChangesRecord> {
    val path = Paths.get(changesPath)
    val effectivePath = if (path.isAbsolute) path else config.projectPath.resolve(path)
    if (!effectivePath.exists()) throw QodanaException(
      QodanaBundle.message("teamcity.changes.file.is.absent", effectivePath.absolutePathString()))
    return effectivePath.readLines().map { parseLine(it) }
  }

  private fun parseLine(line: String): TeamcityChangesRecord {
    val split = line.split(":")
    if (split.size != 3) throw QodanaException(QodanaBundle.message("wrong.teamcity.changes.file.line.format", line))
    val status = try {
      TeamcityChangeStatus.valueOf(split[1])
    }
    catch (ignored: IllegalArgumentException) {
      throw QodanaException(QodanaBundle.message("wrong.teamcity.changes.file.line.format", line))
    }
    return TeamcityChangesRecord(split[0], status, split[2])
  }
}

private class TeamcityScript(runContextFactory: QodanaRunContextFactory, analysisKind: AnalysisKind): DefaultScript(runContextFactory, analysisKind) {
  override suspend fun createGlobalInspectionContext(runContext: QodanaRunContext): QodanaGlobalInspectionContext {
    return runContext.createGlobalInspectionContext(coverageComputationState = QodanaCoverageComputationState.SKIP_COMPUTE)
  }
}

private class TeamcityRunContextFactory(
  private val delegate: QodanaRunContextFactory,
  private val changes: List<TeamcityChangesRecord>
) : QodanaRunContextFactory {

  override suspend fun openRunContext(): QodanaRunContext {
    val sourceContext = delegate.openRunContext()
    val recordMap = changes.filter { it.status != TeamcityChangeStatus.REMOVED }
      .associateBy { Path(it.relativePath) }
    val scopeBuffer = StringBuilder()
    scopeBuffer.appendLine("Script teamcity-changes scope:")

    val res = sourceContext.applyExternalFileScope(
      recordMap.keys,
      onFileIncluded = { file ->
        val relPath = file.toNioPathOrNull()?.relativeTo(sourceContext.config.projectPath)
        recordMap[relPath]?.let(scopeBuffer::appendLine)
      }
    )
    res.project.serviceAsync<LocalChangesService>()
      .isIncrementalAnalysis
      .set(true)
    sourceContext.messageReporter.reportMessage(1, scopeBuffer.toString())
    return res
  }
}

private enum class TeamcityChangeStatus {
  ADDED,
  CHANGED,
  REMOVED
}

private data class TeamcityChangesRecord(val relativePath: String, val status: TeamcityChangeStatus, val hash: String)
