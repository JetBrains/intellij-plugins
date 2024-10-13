package org.jetbrains.qodana.highlight

import com.intellij.openapi.project.Project
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.qodana.problem.SarifProblem
import org.jetbrains.qodana.problem.SarifProblemWithProperties
import org.jetbrains.qodana.report.AggregatedReportMetadata
import org.jetbrains.qodana.report.ReportDescriptor
import org.jetbrains.qodana.settings.ConfigExcludeItem
import java.nio.file.Path
import java.time.Instant

/**
 * Selected report data, to obtain the instance of this interface see [QodanaHighlightedReportService.highlightedReportState]
 */
interface HighlightedReportData {
  data class VcsData(val branch: String?, val revision: String?)

  data class IdeRunData(val ideRunTimestamp: Long?)

  val project: Project

  // TODO – maybe improve heuristic how this is determined
  val isMatchingForProject: Boolean

  /** descriptor of report which is highlighted */
  val sourceReportDescriptor: ReportDescriptor

  val allProblems: Set<SarifProblem>

  val reportMetadata: AggregatedReportMetadata

  val reportName: String

  val jobUrl: String?

  val vcsData: VcsData

  val ideRunData: IdeRunData?

  val createdAt: Instant?

  val excludedDataFlow: StateFlow<Set<ConfigExcludeItem>>

  val inspectionsInfoProvider: InspectionInfoProvider

  val sarifProblemPropertiesProvider: StateFlow<SarifProblemPropertiesProvider>

  val problemToNavigateFlow: Flow<SarifProblem>

  /**
   * Flow of the problems updated with actual properties (presense in file, actual line, column)
   */
  val updatedProblemsPropertiesFlow: Flow<Set<SarifProblemWithProperties>>

  /**
   * Emits problems with actual properties to [updatedProblemsPropertiesFlow],
   * may not actually emit problems, if ones were already emitted on the previous call
   */
  fun updateProblemsProperties(sarifProblemPropertiesUpdaters: List<SarifProblemPropertiesUpdater>)

  /**
   * Problems from [allProblems] which are associated with [filePath] (determined by artifact location's uri in SARIF)
   */
  fun getRelevantProblemsByFilePath(projectDir: Path, filePath: Path, isDeleteEvent: Boolean = false): List<SarifProblem>

  fun requestNavigateToProblem(sarifProblem: SarifProblem)

  suspend fun excludeData(data: ConfigExcludeItem)
}