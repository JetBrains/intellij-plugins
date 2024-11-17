package org.jetbrains.qodana.highlight

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.events.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.*
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.problem.SarifProblem
import org.jetbrains.qodana.problem.SarifProblemWithProperties
import org.jetbrains.qodana.report.AggregatedReportMetadata
import org.jetbrains.qodana.report.LoadedReport
import org.jetbrains.qodana.report.ReportDescriptor
import org.jetbrains.qodana.settings.ConfigExcludeItem
import org.jetbrains.qodana.stats.ProblemStatus
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.Path

class HighlightedReportDataImpl private constructor(
  override val project: Project,
  override val isMatchingForProject: Boolean,
  override val sourceReportDescriptor: ReportDescriptor,
  override val allProblems: Set<SarifProblem>,
  private val problemsByRelativeFilePath: Map<Path, List<SarifProblem>>,
  override val inspectionsInfoProvider: InspectionInfoProvider,
  override val reportMetadata: AggregatedReportMetadata,
  override val reportName: String,
  override val jobUrl: String?,
  override val vcsData: HighlightedReportData.VcsData,
  override val ideRunData: HighlightedReportData.IdeRunData?,
  override val createdAt: Instant?
) : HighlightedReportData {
  companion object {
    suspend fun create(project: Project, sourceReportDescriptor: ReportDescriptor, loadedReport: LoadedReport.Sarif): HighlightedReportDataImpl {
      val validatedSarif = loadedReport.validatedSarif
      return withContext(QodanaDispatchers.Default) {
        val problems: List<SarifProblem> = SarifProblem.fromReport(project, loadedReport.validatedSarif, project.guessProjectDir()?.path)
        val isMatchingForProject = async {
          problems.isEmpty() || isAnySarifProblemMatchingProject(project, problems)
        }
        val problemsByRelativeFilePath = async {
          problems.groupBy { Path(it.relativePathToFile) }
        }
        val inspectionsInfoProvider = async {
          val inspectionsIds = problems.map { it.inspectionId }
          InspectionInfoProvider.create(project, inspectionsIds, validatedSarif.tools)
        }

        HighlightedReportDataImpl(
          project,
          isMatchingForProject.await(),
          sourceReportDescriptor,
          problems.toSet(),
          problemsByRelativeFilePath.await(),
          inspectionsInfoProvider.await(),
          loadedReport.aggregatedReportMetadata,
          loadedReport.reportName,
          validatedSarif.jobUrl,
          HighlightedReportData.VcsData(validatedSarif.branch, validatedSarif.revision),
          HighlightedReportData.IdeRunData(validatedSarif.runTimestamp),
          validatedSarif.createdAt
        )
      }
    }
  }

  private val _excludedDataFlow: MutableStateFlow<Set<ConfigExcludeItem>> = MutableStateFlow(mutableSetOf())
  override val excludedDataFlow: StateFlow<Set<ConfigExcludeItem>> = _excludedDataFlow.asStateFlow()

  private val _excludedDataUpdateChannel = Channel<ConfigExcludeItem>()

  override suspend fun excludeData(data: ConfigExcludeItem) {
    _excludedDataUpdateChannel.send(data)
  }

  private val _sarifProblemPropertiesProvider = MutableStateFlow<SarifProblemPropertiesProvider>(
    MutableSarifProblemPropertiesProvider(allProblems, mutableMapOf())
  )
  override val sarifProblemPropertiesProvider: StateFlow<SarifProblemPropertiesProvider> = _sarifProblemPropertiesProvider.asStateFlow()

  private val _propertiesUpdatersFlow = MutableSharedFlow<List<SarifProblemPropertiesUpdater>>(
    extraBufferCapacity = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
  )

  private val _problemToNavigateFlow = MutableSharedFlow<SarifProblem>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
  override val problemToNavigateFlow: Flow<SarifProblem> = _problemToNavigateFlow.asSharedFlow()

  private val _updatedProblemsPropertiesFlow = MutableSharedFlow<Set<SarifProblemWithProperties>>()
  override val updatedProblemsPropertiesFlow: Flow<Set<SarifProblemWithProperties>> = _updatedProblemsPropertiesFlow.asSharedFlow()

  suspend fun processEvents() {
    supervisorScope {
      launch(QodanaDispatchers.Default) {
        val propertiesProvider = MutableSarifProblemPropertiesProvider(allProblems, mutableMapOf())
        merge(
          _propertiesUpdatersFlow,
          createUpdatersFromVfsEvents()
        ).collect { sarifProblemUpdaters ->
          val updatedSarifProblemActualProperties = sarifProblemUpdaters.mapNotNull {
            val oldProperties = propertiesProvider.getProblemProperties(it.sarifProblem)

            val updated = propertiesProvider.updateProblemProperties(it)
            if (updated == null) return@mapNotNull null

            val newProperties = updated.properties

            // Stats for problem status changes
            if (oldProperties.isMissing != newProperties.isMissing) {
              val newStatus = if (newProperties.isMissing) ProblemStatus.DISAPPEARED else ProblemStatus.APPEARED
              QodanaPluginStatsCounterCollector.STATUS_CHANGED.log(it.sarifProblem.inspectionId, newStatus)
            }
            if (oldProperties.isFixed != newProperties.isFixed) {
              val newStatus = if (newProperties.isFixed) ProblemStatus.FIXED else ProblemStatus.NOT_FIXED
              QodanaPluginStatsCounterCollector.STATUS_CHANGED.log(it.sarifProblem.inspectionId, newStatus)
            }

            updated
          }
          if (updatedSarifProblemActualProperties.isEmpty()) return@collect

          _sarifProblemPropertiesProvider.value = propertiesProvider.toImmutableCopy()
          _updatedProblemsPropertiesFlow.emit(updatedSarifProblemActualProperties.toSet())
        }
      }
      launch(QodanaDispatchers.Default) {
        _excludedDataUpdateChannel.receiveAsFlow().collect { data ->
          val set = _excludedDataFlow.value.toMutableSet()
          set.add(data)
          _excludedDataFlow.value = set
        }
      }
    }
  }

  override fun requestNavigateToProblem(sarifProblem: SarifProblem) {
    _problemToNavigateFlow.tryEmit(sarifProblem)
  }

  override fun updateProblemsProperties(sarifProblemPropertiesUpdaters: List<SarifProblemPropertiesUpdater>) {
    _propertiesUpdatersFlow.tryEmit(sarifProblemPropertiesUpdaters)
  }

  override fun getRelevantProblemsByFilePath(projectDir: Path, filePath: Path, isDeleteEvent: Boolean): List<SarifProblem> {
    val absoluteProjectDir = projectDir.toAbsolutePath().normalize()
    val absoluteFilePath = filePath.toAbsolutePath().normalize()

    val relativeFilePath = try {
      absoluteProjectDir.relativize(absoluteFilePath)
    } catch (e: IllegalArgumentException) {
      return emptyList()
    }

    if (isDeleteEvent) {
      return problemsByRelativeFilePath.filter { it.key.startsWith(relativeFilePath) }.values.flatten()
    }
    return problemsByRelativeFilePath[relativeFilePath] ?: return emptyList()
  }

  private fun createUpdatersFromVfsEvents(): Flow<Set<SarifProblemPropertiesUpdater>> {
    val listenerDisposable = Disposer.newDisposable("Qodana SARIF problems files listener")
    val sarifProblemsWithPresentStatusFlow: Flow<List<Pair<SarifProblem, Boolean>>> = callbackFlow {
      val listener = object : AsyncFileListener {
        override fun prepareChange(events: MutableList<out VFileEvent>): AsyncFileListener.ChangeApplier? {
          val projectDir = project.guessProjectDir()?.path?.let { Path(it) } ?: return null

          val disappearedSarifProblems = events
            .mapNotNull { getDisappearedFileFromEvent(it) }
            .flatMap { getRelevantProblemsByFilePath(projectDir, Path(it), isDeleteEvent = true) }

          trySendBlocking(disappearedSarifProblems.map { it to false })

          val appearedSarifProblems = events
            .mapNotNull { getAppearedFileFromEvent(it) }
            .flatMap { getRelevantProblemsByFilePath(projectDir, Path(it)) }

          if (appearedSarifProblems.isEmpty()) return null

          return object : AsyncFileListener.ChangeApplier {
            override fun afterVfsChange() {
              trySendBlocking(appearedSarifProblems.map { it to true })
            }
          }
        }
      }
      VirtualFileManager.getInstance().addAsyncFileListener(listener, listenerDisposable)
      awaitClose { Disposer.dispose(listenerDisposable) }
    }

    return sarifProblemsWithPresentStatusFlow.map {
      it.map { (sarifProblem, isPresent) ->
        SarifProblemPropertiesUpdater(sarifProblem) { properties -> properties.copy(isPresent = isPresent) }
      }.toSet()
    }
  }
}

private fun getDisappearedFileFromEvent(event: VFileEvent): String? {
  return when (event) {
    is VFileDeleteEvent -> event.path
    is VFileMoveEvent -> event.oldPath
    is VFilePropertyChangeEvent -> {
      if (!event.isRename) return null
      event.oldPath
    }
    else -> return null
  }
}

private fun getAppearedFileFromEvent(event: VFileEvent): String? {
  return when (event) {
    is VFileCreateEvent -> event.path
    is VFileMoveEvent -> event.newPath
    is VFilePropertyChangeEvent -> {
      if (!event.isRename) return null
      event.newPath
    }
    else -> return null
  }
}

private suspend fun isAnySarifProblemMatchingProject(project: Project, problems: List<SarifProblem>): Boolean {
  return withContext(QodanaDispatchers.Default) {
    problems.withIndex().any {
      if (it.index % 10000 == 0) {
        yield()
      }
      it.value.getVirtualFile(project) != null
    }
  }
}