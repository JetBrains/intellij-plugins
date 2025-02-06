package org.jetbrains.qodana.run

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.io.findOrCreateFile
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.io.copy
import com.intellij.util.io.createDirectories
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.cloud.StateManager
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.report.guid
import org.jetbrains.qodana.staticAnalysis.inspections.config.QODANA_YAML_CONFIG_FILENAME
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaYamlFiles
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaInspectionProfileLoader
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaMessageReporter
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunContext
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunner
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.LoadedProfile
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.QodanaInIdeRunContextFactory
import org.jetbrains.qodana.staticAnalysis.profile.QODANA_BASE_PROFILE_NAME
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfile
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfileManager
import org.jetbrains.qodana.staticAnalysis.scopes.QodanaAnalysisScope
import org.jetbrains.qodana.stats.AnalysisState
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText

class QodanaRunInIdeServiceImpl(private val project: Project, private val scope: CoroutineScope) : QodanaRunInIdeService {
  private val stateManager = StateManager<QodanaRunState> { NotRunningImpl() }

  override val runState: StateFlow<QodanaRunState> = stateManager.state

  override val runsResults: StateFlow<Set<QodanaInIdeOutput>> = createRunsResultsFlow()

  init {
    scope.launch(QodanaDispatchers.Default) {
      runState.collectLatest { runState ->
        when(runState) {
          is RunningImpl -> {
            supervisorScope {
              launch {
                runState.launchQodana()
              }
            }
          }
          else -> {}
        }
      }
    }
  }

  private fun createRunsResultsFlow(): StateFlow<Set<QodanaInIdeOutput>> {
    var results = emptySet<QodanaInIdeOutput>()
    return runState
      .filterIsInstance<QodanaRunState.Running>()
      .onEach { runState ->
        val output = runState.outputFuture.await()
        results = results + setOfNotNull(output)
      }
      .map {
        results
      }
      .flowOn(QodanaDispatchers.Default)
      .stateIn(scope, SharingStarted.Eagerly, results)
  }

  private inner class RunningImpl(
    val runInIdeParameters: RunInIdeParameters,
  ) : QodanaRunState.Running {
    private val _outputFuture = CompletableDeferred<QodanaInIdeOutput?>()
    override val outputFuture: Deferred<QodanaInIdeOutput?> = _outputFuture

    suspend fun launchQodana(): QodanaInIdeOutput? {
      val timeAnalysisStarted = System.currentTimeMillis()
      try {
        logAnalysisStateStats(AnalysisState.STARTED, 0L)
        val qodanaInIdeOutput = runQodana()
        _outputFuture.complete(qodanaInIdeOutput)
        logAnalysisStateStats(AnalysisState.SUCCEEDED, System.currentTimeMillis() - timeAnalysisStarted)
        return qodanaInIdeOutput
      }
      catch (e: Throwable) {
        val duration = System.currentTimeMillis() - timeAnalysisStarted
        val state = if (e is CancellationException) {
          AnalysisState.CANCELLED
        } else {
          AnalysisState.FAILED
        }
        logAnalysisStateStats(state, duration)

        _outputFuture.complete(null)
        throw e
      }
      finally {
        stateManager.changeState(this, NotRunningImpl())
      }
    }

    private fun logAnalysisStateStats(state: AnalysisState, durationMillis: Long) {
      QodanaPluginStatsCounterCollector.ANALYSIS_STEP.log(project, state, durationMillis)
    }

    override fun cancel() {
      stateManager.changeState(this, NotRunningImpl())
    }

    @Suppress("DialogTitleCapitalization")
    private suspend fun runQodana(): QodanaInIdeOutput? {
      return withBackgroundProgress(project, QodanaBundle.message("qodana.running.progress.text")) {
        withContext(QodanaDispatchers.Ui) {
          PsiDocumentManager.getInstance(project).commitAllDocuments()
        }
        withContext(QodanaDispatchers.IO) {
          val runContext = createQodanaRunContext(this) ?: return@withContext null
          val config = runContext.config
          val script = QodanaInIdeScript(runContext)
          val qodanaRunner = QodanaRunner(script, config, QodanaMessageReporter.EMPTY)

          writeQodanaYaml(runContext.config.outPath.resolve("log"))
          writeQodanaYaml(Path(PathManager.getLogPath()))

          qodanaRunner.run()
          qodanaRunner.writeFullSarifReport()

          val guid = qodanaRunner.sarif.guid
          this.coroutineContext.job.cancelChildren()
          QodanaInIdeOutput(guid, runContext.config.outPath)
        }
      }
    }

    private suspend fun createQodanaRunContext(scope: CoroutineScope): QodanaRunContext? {
      val projectPath = project.guessProjectDir()?.toNioPath()?.toAbsolutePath() ?: return null
      val outDir = FileUtil.createTempDirectory("qodana_output", null, true)
      val resultsDir = FileUtil.createTempDirectory("qodana_results", null, true)

      val yamlFiles = runInIdeParameters.qodanaYamlFile?.let { QodanaYamlFiles.noConfigDir(it) } ?: QodanaYamlFiles.noFiles()

      val config = QodanaConfig.fromYaml(
        projectPath = projectPath,
        outPath = outDir.toPath(),
        resultsStorage = resultsDir.toPath(),
        yaml = runInIdeParameters.qodanaYamlConfig,
        yamlFiles = yamlFiles,
        baseline = runInIdeParameters.qodanaBaseline?.toString()
      )

      val analysisScope = QodanaAnalysisScope(GlobalSearchScope.projectScope(project), project)
      val loadedInspectionProfile = loadInspectionProfile(config, QodanaInspectionProfileLoader(project))

      return QodanaInIdeRunContextFactory(
        config,
        QodanaMessageReporter.EMPTY,
        project,
        loadedInspectionProfile,
        analysisScope,
        scope
      ).openRunContext()
    }

    private fun loadInspectionProfile(
      config: QodanaConfig,
      inspectionProfileLoader: QodanaInspectionProfileLoader
    ): LoadedProfile {
      val (path, name) = config.profile
      val profileFromQodanaYaml = inspectionProfileLoader.tryLoadProfileByNameOrPath(name, path, QODANA_YAML_CONFIG_FILENAME) {}
      if (profileFromQodanaYaml != null) return LoadedProfile(profileFromQodanaYaml, name, path)

      val defaultProfile = inspectionProfileLoader.tryLoadProfileByNameOrPath(config.defaultProfileName, "", "default profile") {}
      if (defaultProfile != null) return LoadedProfile(defaultProfile, config.defaultProfileName, "")

      val inspectionProfileManager = QodanaInspectionProfileManager.getInstance(project)
      val qodanaBaseProfile = QodanaInspectionProfile.newWithEnabledByDefaultTools(QODANA_BASE_PROFILE_NAME, inspectionProfileManager)
      return LoadedProfile(qodanaBaseProfile, "Current profile", "")
    }

    private suspend fun writeQodanaYaml(destination: Path) {
      try {
        val qodanaYamlInDestination = destination.resolve(QODANA_YAML_CONFIG_FILENAME)
        runInterruptible(QodanaDispatchers.IO) {
          destination.createDirectories()
          when {
            runInIdeParameters.qodanaYamlFile != null -> {
              qodanaYamlInDestination.deleteIfExists()
              runInIdeParameters.qodanaYamlFile.copy(qodanaYamlInDestination)
            }
            runInIdeParameters.qodanaYamlConfigText != null -> {
              qodanaYamlInDestination.findOrCreateFile()
              qodanaYamlInDestination.writeText(runInIdeParameters.qodanaYamlConfigText)
            }
            else -> {}
          }
        }
      }
      catch (e : IOException) {
        thisLogger().warn("Failed writing qodana yaml", e)
      }
    }
  }

  private inner class NotRunningImpl : QodanaRunState.NotRunning {
    override fun run(runInIdeParameters: RunInIdeParameters): QodanaRunState.Running? {
      return stateManager.changeState(this, RunningImpl(runInIdeParameters))
    }
  }
}
