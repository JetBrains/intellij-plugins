package org.jetbrains.qodana.ui.run

import com.intellij.mock.MockDocument
import com.intellij.openapi.editor.impl.ImaginaryEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.testFramework.DumbModeTestUtils
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.cloud.api.mockQDCloudHttpClient
import org.jetbrains.qodana.cloud.api.respond
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.dispatchAllTasksOnUi
import org.jetbrains.qodana.qodanaPluginLightFixture
import org.jetbrains.qodana.respond200PublishReport
import org.jetbrains.qodana.run.QodanaInIdeOutput
import org.jetbrains.qodana.run.QodanaRunInIdeService
import org.jetbrains.qodana.run.QodanaRunState
import org.jetbrains.qodana.run.RunInIdeParameters
import org.jetbrains.qodana.runDispatchingOnUi
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaYamlConfig
import org.jetbrains.qodana.ui.ProjectVcsDataProviderMock
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.copyTo

@TestApplication
class LocalRunQodanaViewModelTest {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)
  }

  private val qodanaFixture by qodanaPluginLightFixture(projectFixture)

  private val emptyProjectVcsDataProvider = ProjectVcsDataProviderMock()

  @Test
  fun `test save yaml state is initially save`(): Unit = runDispatchingOnUi {
    val yamlViewModelMock = QodanaYamlViewModelMock()
    val viewModel = LocalRunQodanaViewModel(qodanaFixture.project, qodanaFixture.scope, yamlViewModelMock, emptyProjectVcsDataProvider)

    viewModel.saveQodanaYamlStateFlow.first { it == LocalRunQodanaViewModel.SaveQodanaYamlState.SAVE }
  }

  @Test
  fun `test save yaml state is already physical when yaml is physical`(): Unit = runDispatchingOnUi {
    val yamlViewModelMock = QodanaYamlViewModelMock()
    val viewModel = LocalRunQodanaViewModel(qodanaFixture.project, qodanaFixture.scope, yamlViewModelMock, emptyProjectVcsDataProvider)

    yamlViewModelMock.yamlStateFlow.value = yamlStateMock(isPhysical = true)
    viewModel.saveQodanaYamlStateFlow.first { it == LocalRunQodanaViewModel.SaveQodanaYamlState.ALREADY_PHYSICAL }
  }

  @Test
  fun `test set save yaml state to false`(): Unit = runDispatchingOnUi {
    val yamlViewModelMock = QodanaYamlViewModelMock()
    val viewModel = LocalRunQodanaViewModel(qodanaFixture.project, qodanaFixture.scope, yamlViewModelMock, emptyProjectVcsDataProvider)

    viewModel.setSaveQodanaYaml(false)
    viewModel.saveQodanaYamlStateFlow.first { it == LocalRunQodanaViewModel.SaveQodanaYamlState.NO_SAVE }
  }

  @Test
  fun `test save yaml state to false and to true`(): Unit = runDispatchingOnUi {
    val yamlViewModelMock = QodanaYamlViewModelMock()
    val viewModel = LocalRunQodanaViewModel(qodanaFixture.project, qodanaFixture.scope, yamlViewModelMock, emptyProjectVcsDataProvider)

    viewModel.setSaveQodanaYaml(false)
    viewModel.saveQodanaYamlStateFlow.first { it == LocalRunQodanaViewModel.SaveQodanaYamlState.NO_SAVE }

    viewModel.setSaveQodanaYaml(true)
    viewModel.saveQodanaYamlStateFlow.first { it == LocalRunQodanaViewModel.SaveQodanaYamlState.SAVE }
  }

  @Test
  fun `test save yaml state to false or to true has no effect when yaml is physical`(): Unit = runDispatchingOnUi {
    val yamlViewModelMock = QodanaYamlViewModelMock()
    val viewModel = LocalRunQodanaViewModel(qodanaFixture.project, qodanaFixture.scope, yamlViewModelMock, emptyProjectVcsDataProvider)
    yamlViewModelMock.yamlStateFlow.value = yamlStateMock(isPhysical = true)

    viewModel.saveQodanaYamlStateFlow.first { it == LocalRunQodanaViewModel.SaveQodanaYamlState.ALREADY_PHYSICAL }

    var lastSaveState: LocalRunQodanaViewModel.SaveQodanaYamlState? = null
    qodanaFixture.scope.launch(QodanaDispatchers.Ui){
      viewModel.saveQodanaYamlStateFlow.collect {
        lastSaveState = it
      }
    }
    dispatchAllTasksOnUi()
    assertThat(lastSaveState).isEqualTo(LocalRunQodanaViewModel.SaveQodanaYamlState.ALREADY_PHYSICAL)

    viewModel.setSaveQodanaYaml(false)
    dispatchAllTasksOnUi()
    assertThat(lastSaveState).isEqualTo(LocalRunQodanaViewModel.SaveQodanaYamlState.ALREADY_PHYSICAL)

    viewModel.setSaveQodanaYaml(true)
    dispatchAllTasksOnUi()
    assertThat(lastSaveState).isEqualTo(LocalRunQodanaViewModel.SaveQodanaYamlState.ALREADY_PHYSICAL)
  }

  @Test
  fun `test set cloud token`(): Unit = runDispatchingOnUi {
    val yamlViewModelMock = QodanaYamlViewModelMock()
    val viewModel = LocalRunQodanaViewModel(qodanaFixture.project, qodanaFixture.scope, yamlViewModelMock, emptyProjectVcsDataProvider)

    val token = UUID.randomUUID().toString()
    viewModel.setCloudToken(token)
    viewModel.cloudTokenStateFlow.first { it == token }
  }

  @Test
  fun `test set cloud token twice`(): Unit = runDispatchingOnUi {
    val yamlViewModelMock = QodanaYamlViewModelMock()
    val viewModel = LocalRunQodanaViewModel(qodanaFixture.project, qodanaFixture.scope, yamlViewModelMock, emptyProjectVcsDataProvider)

    val token = UUID.randomUUID().toString()
    viewModel.setCloudToken(token)
    viewModel.cloudTokenStateFlow.first { it == token }

    val token2 = UUID.randomUUID().toString()
    viewModel.setCloudToken(token2)
    viewModel.cloudTokenStateFlow.first { it == token2 }
  }

  @Test
  fun `test set do publish to cloud initially false`(): Unit = runDispatchingOnUi {
    val yamlViewModelMock = QodanaYamlViewModelMock()
    val viewModel = LocalRunQodanaViewModel(qodanaFixture.project, qodanaFixture.scope, yamlViewModelMock, emptyProjectVcsDataProvider)

    assertThat(viewModel.publishToCloudStateFlow.value).isFalse()
  }

  @Test
  fun `test set true do publish to cloud`(): Unit = runDispatchingOnUi {
    val yamlViewModelMock = QodanaYamlViewModelMock()
    val viewModel = LocalRunQodanaViewModel(qodanaFixture.project, qodanaFixture.scope, yamlViewModelMock, emptyProjectVcsDataProvider)

    viewModel.setPublishToCloud(true)
    viewModel.publishToCloudStateFlow.first { it }
  }

  @Test
  fun `test set true publish to cloud then set false`(): Unit = runDispatchingOnUi {
    val yamlViewModelMock = QodanaYamlViewModelMock()
    val viewModel = LocalRunQodanaViewModel(qodanaFixture.project, qodanaFixture.scope, yamlViewModelMock, emptyProjectVcsDataProvider)

    viewModel.setPublishToCloud(true)
    viewModel.publishToCloudStateFlow.first { it }

    viewModel.setPublishToCloud(false)
    viewModel.publishToCloudStateFlow.first { !it }
  }

  @Test
  fun `test can finish initially`(): Unit = runDispatchingOnUi {
    val yamlViewModelMock = QodanaYamlViewModelMock()
    val viewModel = LocalRunQodanaViewModel(qodanaFixture.project, qodanaFixture.scope, yamlViewModelMock, emptyProjectVcsDataProvider)

    var lastCanFinish: Boolean? = null
    qodanaFixture.scope.launch(QodanaDispatchers.Ui){
      viewModel.isFinishAvailableFlow.collect {
        lastCanFinish = it
      }
    }
    dispatchAllTasksOnUi()
    assertThat(lastCanFinish).isTrue()
  }

  @Test
  fun `test can't finish when publish is true and token is empty`(): Unit = runDispatchingOnUi {
    val yamlViewModelMock = QodanaYamlViewModelMock()
    val viewModel = LocalRunQodanaViewModel(qodanaFixture.project, qodanaFixture.scope, yamlViewModelMock, emptyProjectVcsDataProvider)
    viewModel.setPublishToCloud(true)
    viewModel.setCloudToken("")

    var lastCanFinish: Boolean? = null
    qodanaFixture.scope.launch(QodanaDispatchers.Ui){
      viewModel.isFinishAvailableFlow.collect {
        lastCanFinish = it
      }
    }
    dispatchAllTasksOnUi()
    assertThat(lastCanFinish).isFalse()
  }

  @Test
  fun `test can finish when publish is true and token is not empty`(): Unit = runDispatchingOnUi {
    val yamlViewModelMock = QodanaYamlViewModelMock()
    val viewModel = LocalRunQodanaViewModel(qodanaFixture.project, qodanaFixture.scope, yamlViewModelMock, emptyProjectVcsDataProvider)
    viewModel.setPublishToCloud(true)
    viewModel.setCloudToken("not empty token")

    var lastCanFinish: Boolean? = null
    qodanaFixture.scope.launch(QodanaDispatchers.Ui){
      viewModel.isFinishAvailableFlow.collect {
        lastCanFinish = it
      }
    }
    dispatchAllTasksOnUi()
    assertThat(lastCanFinish).isTrue()
  }

  @Test
  fun `test can't finish when in dumb mode`(): Unit = runDispatchingOnUi {
    val yamlViewModelMock = QodanaYamlViewModelMock()
    val viewModel = LocalRunQodanaViewModel(qodanaFixture.project, qodanaFixture.scope, yamlViewModelMock, emptyProjectVcsDataProvider)

    DumbModeTestUtils.runInDumbModeSynchronously(qodanaFixture.project) {
      var lastCanFinish: Boolean? = null
      qodanaFixture.scope.launch(QodanaDispatchers.Ui){
        viewModel.isFinishAvailableFlow.collect {
          lastCanFinish = it
        }
      }
      dispatchAllTasksOnUi()
      assertThat(lastCanFinish).isFalse()
    }
  }

  @Test
  fun `test can't finish when already running`(): Unit = runDispatchingOnUi {
    val runInIdeServiceMock = QodanaRunInIdeServiceMock(qodanaRunningStateMock(null))
    QodanaRunInIdeService.setTestInstance(qodanaFixture.project, runInIdeServiceMock, qodanaFixture.testRootDisposable)

    val yamlViewModelMock = QodanaYamlViewModelMock()
    val viewModel = LocalRunQodanaViewModel(qodanaFixture.project, qodanaFixture.scope, yamlViewModelMock, emptyProjectVcsDataProvider)

    var lastCanFinish: Boolean? = null
    qodanaFixture.scope.launch(QodanaDispatchers.Ui){
      viewModel.isFinishAvailableFlow.collect {
        lastCanFinish = it
      }
    }
    dispatchAllTasksOnUi()
    assertThat(lastCanFinish).isFalse()
  }

  @Test
  fun `test launch analysis and publish`(): Unit = runDispatchingOnUi {
    val qodanaInIdeOutput = prepareQodanaInIdeOutput()
    val notRunningMock = QodanaNotRunningStateMock(qodanaRunningStateMock(qodanaInIdeOutput))
    val runInIdeServiceMock = QodanaRunInIdeServiceMock(notRunningMock)
    QodanaRunInIdeService.setTestInstance(qodanaFixture.project, runInIdeServiceMock, qodanaFixture.testRootDisposable)

    val yamlViewModelMock = QodanaYamlViewModelMock(parseResult = QodanaYamlViewModel.ParseResult.Valid(QodanaYamlConfig.EMPTY_V1))
    yamlViewModelMock.yamlStateFlow.value = yamlStateMock(isPhysical = false)
    val viewModel = LocalRunQodanaViewModel(qodanaFixture.project, qodanaFixture.scope, yamlViewModelMock, emptyProjectVcsDataProvider)
    viewModel.setPublishToCloud(true)
    viewModel.setCloudToken("token")

    val publishToCloudApiMock = QodanaCloudPublishToCloudApiMock()
    publishToCloudApiMock.installOnHttpClient()

    var analysisWasLaunched = false
    qodanaFixture.scope.launch(QodanaDispatchers.Ui){
      viewModel.analysisWasLaunchedFlow.collect {
        analysisWasLaunched = true
      }
    }

    val launchResult = viewModel.launchAnalysis().await() as LocalRunQodanaViewModel.LaunchResult.PublishToCloud
    dispatchAllTasksOnUi()
    assertThat(launchResult.qodanaInIdeOutput).isEqualTo(qodanaInIdeOutput)

    assertThat(yamlViewModelMock.timesParseCalled).isEqualTo(1)
    assertThat(analysisWasLaunched).isTrue()
    assertThat(yamlViewModelMock.timesWriteCalled).isEqualTo(1)
    assertThat(notRunningMock.runWasCalled).isTrue()
    assertThat(publishToCloudApiMock.publishWasCalled).isTrue()
  }

  @Test
  fun `test launch analysis no publish`(): Unit = runDispatchingOnUi {
    val qodanaInIdeOutput = prepareQodanaInIdeOutput()
    val notRunningMock = QodanaNotRunningStateMock(qodanaRunningStateMock(qodanaInIdeOutput))
    val runInIdeServiceMock = QodanaRunInIdeServiceMock(notRunningMock)
    QodanaRunInIdeService.setTestInstance(qodanaFixture.project, runInIdeServiceMock, qodanaFixture.testRootDisposable)

    val yamlViewModelMock = QodanaYamlViewModelMock(parseResult = QodanaYamlViewModel.ParseResult.Valid(QodanaYamlConfig.EMPTY_V1))
    yamlViewModelMock.yamlStateFlow.value = yamlStateMock(isPhysical = false)
    val viewModel = LocalRunQodanaViewModel(qodanaFixture.project, qodanaFixture.scope, yamlViewModelMock, emptyProjectVcsDataProvider)

    val publishToCloudApiMock = QodanaCloudPublishToCloudApiMock()
    publishToCloudApiMock.installOnHttpClient()

    var analysisWasLaunched = false
    qodanaFixture.scope.launch(QodanaDispatchers.Ui){
      viewModel.analysisWasLaunchedFlow.collect {
        analysisWasLaunched = true
      }
    }

    val launchResult = viewModel.launchAnalysis().await() as LocalRunQodanaViewModel.LaunchResult.NoPublishToCloud
    dispatchAllTasksOnUi()
    assertThat(launchResult.qodanaInIdeOutput).isEqualTo(qodanaInIdeOutput)

    assertThat(yamlViewModelMock.timesParseCalled).isEqualTo(1)
    assertThat(analysisWasLaunched).isTrue()
    assertThat(yamlViewModelMock.timesWriteCalled).isEqualTo(1)
    assertThat(notRunningMock.runWasCalled).isTrue()
    assertThat(publishToCloudApiMock.publishWasCalled).isFalse()
  }

  @Test
  fun `test launch analysis no yaml write`(): Unit = runDispatchingOnUi {
    val qodanaInIdeOutput = prepareQodanaInIdeOutput()
    val notRunningMock = QodanaNotRunningStateMock(qodanaRunningStateMock(qodanaInIdeOutput))
    val runInIdeServiceMock = QodanaRunInIdeServiceMock(notRunningMock)
    QodanaRunInIdeService.setTestInstance(qodanaFixture.project, runInIdeServiceMock, qodanaFixture.testRootDisposable)

    val yamlViewModelMock = QodanaYamlViewModelMock(parseResult = QodanaYamlViewModel.ParseResult.Valid(QodanaYamlConfig.EMPTY_V1))
    val viewModel = LocalRunQodanaViewModel(qodanaFixture.project, qodanaFixture.scope, yamlViewModelMock, emptyProjectVcsDataProvider)
    yamlViewModelMock.yamlStateFlow.value = yamlStateMock(isPhysical = false)
    viewModel.saveQodanaYamlStateFlow.first { it == LocalRunQodanaViewModel.SaveQodanaYamlState.SAVE }
    viewModel.setSaveQodanaYaml(false)

    val publishToCloudApiMock = QodanaCloudPublishToCloudApiMock()
    publishToCloudApiMock.installOnHttpClient()

    var analysisWasLaunched = false
    qodanaFixture.scope.launch(QodanaDispatchers.Ui){
      viewModel.analysisWasLaunchedFlow.collect {
        analysisWasLaunched = true
      }
    }

    val launchResult = viewModel.launchAnalysis().await() as LocalRunQodanaViewModel.LaunchResult.NoPublishToCloud
    dispatchAllTasksOnUi()
    assertThat(launchResult.qodanaInIdeOutput).isEqualTo(qodanaInIdeOutput)

    assertThat(yamlViewModelMock.timesParseCalled).isEqualTo(1)
    assertThat(analysisWasLaunched).isTrue()
    assertThat(yamlViewModelMock.timesWriteCalled).isEqualTo(0)
    assertThat(notRunningMock.runWasCalled).isTrue()
    assertThat(publishToCloudApiMock.publishWasCalled).isFalse()
  }

  @Test
  fun `test launch analysis parse failed`(): Unit = runDispatchingOnUi {
    val qodanaInIdeOutput = prepareQodanaInIdeOutput()
    val notRunningMock = QodanaNotRunningStateMock(qodanaRunningStateMock(qodanaInIdeOutput))
    val runInIdeServiceMock = QodanaRunInIdeServiceMock(notRunningMock)
    QodanaRunInIdeService.setTestInstance(qodanaFixture.project, runInIdeServiceMock, qodanaFixture.testRootDisposable)

    val yamlViewModelMock = QodanaYamlViewModelMock(parseResult = QodanaYamlViewModel.ParseResult.Error("error"))
    yamlViewModelMock.yamlStateFlow.value = yamlStateMock(isPhysical = false)
    val viewModel = LocalRunQodanaViewModel(qodanaFixture.project, qodanaFixture.scope, yamlViewModelMock, emptyProjectVcsDataProvider)

    val publishToCloudApiMock = QodanaCloudPublishToCloudApiMock()
    publishToCloudApiMock.installOnHttpClient()

    var analysisWasLaunched = false
    qodanaFixture.scope.launch(QodanaDispatchers.Ui){
      viewModel.analysisWasLaunchedFlow.collect {
        analysisWasLaunched = true
      }
    }

    val launchResult = viewModel.launchAnalysis().await()
    dispatchAllTasksOnUi()
    assertThat(launchResult).isNull()

    assertThat(yamlViewModelMock.timesParseCalled).isEqualTo(1)
    assertThat(analysisWasLaunched).isFalse()
    assertThat(yamlViewModelMock.timesWriteCalled).isEqualTo(0)
    assertThat(notRunningMock.runWasCalled).isFalse()
    assertThat(publishToCloudApiMock.publishWasCalled).isFalse()
  }

  private inner class QodanaYamlViewModelMock(
    override val yamlStateFlow: MutableStateFlow<QodanaYamlViewModel.YamlState?> = MutableStateFlow(null),
    val parseResult: QodanaYamlViewModel.ParseResult? = null,
    val writeResultPath: Path? = null,
  ) : QodanaYamlViewModel {
    override val yamlValidationErrorFlow: Flow<QodanaYamlViewModel.ParseResult.Error?> = emptyFlow()

    override val project: Project
      get() = qodanaFixture.project

    var timesParseCalled = 0
    var timesWriteCalled = 0

    override fun parseQodanaYaml(): Deferred<QodanaYamlViewModel.ParseResult?> {
      timesParseCalled++
      return CompletableDeferred(parseResult)
    }

    override fun writeQodanaYamlIfNeeded(): Deferred<Path?> {
      timesWriteCalled++
      return CompletableDeferred(writeResultPath)
    }
  }

  private fun yamlStateMock(isPhysical: Boolean): QodanaYamlViewModel.YamlState {
    val mockDocument = MockDocument()
    val physicalFile = if (isPhysical) {
      FileUtilRt.createTempFile("qodana.yaml", "", true).toPath()
    } else {
      null
    }
    return QodanaYamlViewModel.YamlState(mockDocument, ImaginaryEditor(qodanaFixture.project, mockDocument), physicalFile)
  }

  private class QodanaRunInIdeServiceMock(qodanaRunState: QodanaRunState) : QodanaRunInIdeService {
    override val runState: StateFlow<QodanaRunState> = MutableStateFlow(qodanaRunState)

    override val runsResults: StateFlow<Set<QodanaInIdeOutput>> = MutableStateFlow(emptySet())
  }

  private class QodanaCloudPublishToCloudApiMock {
    var publishWasCalled: Boolean = false

    fun installOnHttpClient() {
      mockQDCloudHttpClient.apply {
        respond200PublishReport(
          host = "https://tests-host.com",
          projectId = "ID",
          reportId = "reportId"
        )
        respond("reports/") {
          publishWasCalled = true
          null
        }
      }
    }
  }

  private fun qodanaRunningStateMock(output: QodanaInIdeOutput?): QodanaRunState.Running {
    return object : QodanaRunState.Running {
      override val outputFuture: Deferred<QodanaInIdeOutput?> = CompletableDeferred(output)

      override fun cancel() {
      }
    }
  }

  private class QodanaNotRunningStateMock(private val running: QodanaRunState.Running?) : QodanaRunState.NotRunning {
    var runWasCalled: Boolean = false

    override fun run(runInIdeParameters: RunInIdeParameters): QodanaRunState.Running? {
      runWasCalled = true
      return running
    }
  }

  private fun prepareQodanaInIdeOutput(): QodanaInIdeOutput {
    val sarif = qodanaFixture.sarifTestReports.validForConverter
    val outputDir = FileUtilRt.createTempDirectory("converter", "", true).toPath()
    val sarifInOutput = outputDir.resolve("qodana.sarif.json")
    sarif.copyTo(sarifInOutput)
    return QodanaInIdeOutput("guid", outputDir)
  }
}