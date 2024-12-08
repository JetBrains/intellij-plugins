package org.jetbrains.qodana.ui.run

import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.NlsContexts
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.cloud.QodanaCloudDefaultUrls
import org.jetbrains.qodana.cloud.QodanaCloudStateService
import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.cloud.api.IjQDCloudClient
import org.jetbrains.qodana.cloud.api.getErrorNotification
import org.jetbrains.qodana.cloud.openBrowserWithCurrentQodanaCloudFrontend
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.cloudclient.qodanaCloudResponse
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.isInDumbModeFlow
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.notifications.QodanaNotifications
import org.jetbrains.qodana.publisher.PublishResult
import org.jetbrains.qodana.run.*
import org.jetbrains.qodana.staticAnalysis.inspections.runner.publishToCloud
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.stats.RunDialogYamlState
import org.jetbrains.qodana.ui.ProjectVcsDataProvider
import java.io.File
import java.nio.file.InvalidPathException
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class LocalRunQodanaViewModel(
  val project: Project,
  private val scope: CoroutineScope,
  val qodanaYamlViewModel: QodanaYamlViewModel,
  private val projectVcsDataProvider: ProjectVcsDataProvider,
) {
  sealed interface LaunchResult {
    val qodanaInIdeOutput: QodanaInIdeOutput

    class PublishToCloud(override val qodanaInIdeOutput: QodanaInIdeOutput, val publishedReportLink: String) : LaunchResult

    class NoPublishToCloud(override val qodanaInIdeOutput: QodanaInIdeOutput) : LaunchResult
  }

  enum class SaveQodanaYamlState {
    SAVE,
    NO_SAVE,
    ALREADY_PHYSICAL,
  }

  private val _saveQodanaYamlStateFlow = MutableStateFlow(SaveQodanaYamlState.SAVE)
  val saveQodanaYamlStateFlow: StateFlow<SaveQodanaYamlState> = _saveQodanaYamlStateFlow.asStateFlow()

  private val _publishToCloudStateFlow = MutableStateFlow(false)
  val publishToCloudStateFlow: StateFlow<Boolean> = _publishToCloudStateFlow.asStateFlow()

  private val _cloudTokenStateFlow = MutableStateFlow(InMemorySavedCloudTokenService.getInstance(project).token)
  val cloudTokenStateFlow: StateFlow<String> = _cloudTokenStateFlow.asStateFlow()

  private val defaultBaselineFile: String? = findDefaultBaselineInProjectRoot()

  private val _doUseBaselineStateFlow = MutableStateFlow(defaultBaselineFile != null)
  val doUseBaselineStateFlow: StateFlow<Boolean> = _doUseBaselineStateFlow.asStateFlow()

  private val _baselineFileStateFlow = MutableStateFlow(defaultBaselineFile ?: "")
  val baselineFileStateFlow: StateFlow<String> = _baselineFileStateFlow.asStateFlow()

  val baselineFileErrorMessageFlow: SharedFlow<@NlsContexts.DialogMessage String?> = createBaselineFileErrorMessageFlow()

  private val _analysisWasLaunchedFlow = MutableSharedFlow<Unit>()
  val analysisWasLaunchedFlow: SharedFlow<Unit> = _analysisWasLaunchedFlow.asSharedFlow()

  val isFinishAvailableFlow: Flow<Boolean> = createIsFinishAvailableFlow()

  init {
    scope.launch(QodanaDispatchers.Default) {
      launch {
        qodanaYamlViewModel.yamlStateFlow.filterNotNull().map { it.isPhysical }.collect { isYamlPhysical ->
          _saveQodanaYamlStateFlow.update { currentState ->
            when(currentState) {
              SaveQodanaYamlState.NO_SAVE, SaveQodanaYamlState.SAVE -> {
                if (isYamlPhysical) SaveQodanaYamlState.ALREADY_PHYSICAL else currentState
              }
              SaveQodanaYamlState.ALREADY_PHYSICAL -> {
                if (!isYamlPhysical) SaveQodanaYamlState.SAVE else currentState
              }
            }
          }
        }
      }
      launch {
        try {
          awaitCancellation()
        }
        finally {
          InMemorySavedCloudTokenService.getInstance(project).token = cloudTokenStateFlow.value
        }
      }
    }
  }

  fun openGetTokenPage() {
    openBrowserWithCurrentQodanaCloudFrontend()
  }

  private fun createBaselineFileErrorMessageFlow(): SharedFlow<@NlsContexts.DialogMessage String?> {
    return doUseBaselineStateFlow.flatMapLatest { doUse ->
      if (!doUse) return@flatMapLatest flowOf(null)

      baselineFileStateFlow
        .debounce(100.milliseconds)
        .map { file ->
          validateBaselineFile(file)
        }
    }.distinctUntilChanged().flowOn(QodanaDispatchers.Default).shareIn(scope, SharingStarted.Eagerly, replay = 1)
  }

  private fun validateBaselineFile(file: String): @NlsContexts.DialogMessage String? {
    if (file.isEmpty()) return QodanaBundle.message("local.run.baseline.location.can.t.be.empty")

    val nioPath = try {
      Path(file)
    }
    catch (_ : InvalidPathException) {
      return QodanaBundle.message("local.run.value.not.path")
    }
    val fileName = file.split(File.separator).last()
    val isJsonOrSarifExtension = fileName.endsWith(".json") || fileName.endsWith(".sarif")
    if (!isJsonOrSarifExtension) return QodanaBundle.message("local.run.file.must.be.sarif.json.or.sarif")

    val absoluteNioPath = if (nioPath.isAbsolute) {
      nioPath
    } else {
      project.guessProjectDir()?.toNioPath()?.resolve(nioPath)?.normalize()
    }
    if (absoluteNioPath?.exists() != true) {
      return absoluteNioPath?.let { QodanaBundle.message("local.run.file.does.not.exist", absoluteNioPath) }
             ?: QodanaBundle.message("local.run.file.does.not.exist.no.name")
    }

    return null
  }

  fun launchAnalysis(): Deferred<LaunchResult?> {
    return project.qodanaProjectScope.async(QodanaDispatchers.Default) {
      val doUseBaseline = doUseBaselineStateFlow.value
      val baselineFile = if (doUseBaseline) {
        val file = baselineFileStateFlow.value
        val errorMessage = validateBaselineFile(file)
        if (errorMessage != null) {
          return@async null
        }
        Path(file)
      } else {
        null
      }

      val token = cloudTokenStateFlow.value
      val needToPublish = publishToCloudStateFlow.value

      val yamlState = saveQodanaYamlStateFlow.value

      val parsedYamlConfig = qodanaYamlViewModel.parseQodanaYaml().await() ?: return@async null
      if (parsedYamlConfig !is QodanaYamlViewModel.ParseResult.Valid) return@async null

      logLaunchAnalysisStats(needToPublish, yamlState, baselineFile != null)
      _analysisWasLaunchedFlow.emit(Unit)

      val yamlFile = if (yamlState != SaveQodanaYamlState.NO_SAVE) {
        qodanaYamlViewModel.writeQodanaYamlIfNeeded().await()
      } else {
        null
      }

      val qodanaInIdeOutput = when(val runState = QodanaRunInIdeService.getInstance(project).runState.value) {
        is QodanaRunState.NotRunning -> {
          val runInIdeParameters = RunInIdeParameters(parsedYamlConfig.yamlConfig, parsedYamlConfig.parsedText, yamlFile, baselineFile)
          runState.run(runInIdeParameters)?.outputFuture?.await() ?: return@async null
        }
        is QodanaRunState.Running -> {
          runState.outputFuture.await()
        }
      } ?: return@async null

      val publishedReportLink = if (needToPublish) {
        val userState = QodanaCloudStateService.getInstance().userState.value
        val projectApiResponse = qodanaCloudResponse {
          when(userState) {
            is UserState.Authorized -> userState.cloudClient().value().projectApi(token)
            else -> IjQDCloudClient(QodanaCloudDefaultUrls.websiteUrl).v1().value().projectApi(token)
          }
        }
        val projectApi = when(projectApiResponse) {
          is QDCloudResponse.Success -> projectApiResponse.value
          is QDCloudResponse.Error -> {
            projectApiResponse
              .getErrorNotification(QodanaBundle.message("notification.title.failed.uploading.results.to.qodana.cloud"))
              .notify(project)

            return@async null
          }
        }
        val uploadedReport = publishToCloud(projectApi, qodanaInIdeOutput.path)
        val reportLink = when(uploadedReport) {
          is PublishResult.Success -> {
            uploadedReport.uploadedReport.reportLink
          }
          is PublishResult.Error -> {
            QodanaNotifications.General.notification(
              QodanaBundle.message("notification.title.failed.uploading.results.to.qodana.cloud"),
              uploadedReport.message,
              NotificationType.ERROR
            ).notify(project)
            null
          }
        }
        reportLink
      } else {
        null
      }
      return@async if (publishedReportLink != null) {
        LaunchResult.PublishToCloud(qodanaInIdeOutput, publishedReportLink)
      } else {
        LaunchResult.NoPublishToCloud(qodanaInIdeOutput)
      }
    }
  }

  private fun logLaunchAnalysisStats(
    publishToCloud: Boolean,
    yamlState: SaveQodanaYamlState,
    useBaseline: Boolean
  ) {
    val statsYamlState = when(yamlState) {
      SaveQodanaYamlState.SAVE -> RunDialogYamlState.SAVE
      SaveQodanaYamlState.NO_SAVE -> RunDialogYamlState.NO_SAVE
      SaveQodanaYamlState.ALREADY_PHYSICAL -> RunDialogYamlState.ALREADY_PRESENT
    }
    QodanaPluginStatsCounterCollector.RUN_DIALOG_START_RUN.log(
      project,
      publishToCloud,
      statsYamlState,
      useBaseline
    )
  }

  private fun findDefaultBaselineInProjectRoot(): String? {
    val filename = "qodana.sarif.json"
    project.guessProjectDir()?.findChild(filename) ?: return null
    return filename
  }

  fun setSaveQodanaYaml(doSave: Boolean) {
    _saveQodanaYamlStateFlow.update {
      if (it == SaveQodanaYamlState.ALREADY_PHYSICAL) return@update it
      if (doSave) SaveQodanaYamlState.SAVE else SaveQodanaYamlState.NO_SAVE
    }
  }

  fun setPublishToCloud(doPublish: Boolean) {
    _publishToCloudStateFlow.value = doPublish
  }

  fun setCloudToken(token: String) {
    _cloudTokenStateFlow.value = token
  }

  fun setDoUseBaseline(doUse: Boolean) {
    _doUseBaselineStateFlow.value = doUse
  }

  fun setBaselineFile(path: String) {
    _baselineFileStateFlow.value = path
  }

  private fun createIsFinishAvailableFlow(): Flow<Boolean> {
    val isTokenStateValidFlow = combine(publishToCloudStateFlow, cloudTokenStateFlow, ::Pair).map { (doPublish, token) ->
      val wantToPublishWithEmptyToken = doPublish && token.isEmpty()
      !wantToPublishWithEmptyToken
    }
    return combine(
      isInDumbModeFlow(project).map { !it },
      QodanaRunInIdeService.getInstance(project).runState.map { it is QodanaRunState.NotRunning },
      isTokenStateValidFlow,
      baselineFileErrorMessageFlow.map { it == null },
      transform = { isNotDumb, isNotRunning, isTokenStateValid, isBaselineFileValid, ->
        isNotDumb && isNotRunning && isTokenStateValid && isBaselineFileValid
      }
    )
  }
}

@Service(Service.Level.PROJECT)
private class InMemorySavedCloudTokenService(@Suppress("UNUSED_PARAMETER") project: Project) {
  companion object {
    fun getInstance(project: Project): InMemorySavedCloudTokenService = project.service()
  }

  @Volatile var token: String = ""
}
