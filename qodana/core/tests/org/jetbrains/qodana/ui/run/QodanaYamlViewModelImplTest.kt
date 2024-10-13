package org.jetbrains.qodana.ui.run

import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readText
import com.intellij.platform.util.coroutines.childScope
import com.intellij.testFramework.EditorTestUtil
import com.intellij.testFramework.TestDataPath
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.QodanaPluginHeavyTestBase
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaYamlConfig
import kotlin.io.path.Path
import kotlin.io.path.pathString

@Language("YAML")
private val EXPECTED_VALID_PHYSICAL_YAML_CONTENT = """
  version: 1.0
  profile:
    name: qodana.starter
""".trimIndent()

@OptIn(ExperimentalCoroutinesApi::class)
@TestDataPath("\$CONTENT_ROOT/test-data/QodanaYamlViewModelImplTest")
class QodanaYamlViewModelImplTest : QodanaPluginHeavyTestBase() {
  private val expectedValidYamlConfig = QodanaYamlConfig(
    version = "1.0", profile = QodanaProfileConfig(name = "qodana.starter"))

  override fun getBasePath(): String = Path(super.getBasePath(), "QodanaYamlViewModelImplTest").pathString

  override fun setUp() {
    super.setUp()
    setUpProject()
  }

  private fun setUpProject() {
    invokeAndWaitIfNeeded {
      copyProjectTestData(getTestName(true).trim())
    }
  }

  override fun tearDown() {
    try {
      // releasing of editors in non-cancellable section of coroutine doesn't correspond to .cancelAndJoin on coroutine job,
      // so we release editors explicitly here
      EditorTestUtil.releaseAllEditors()
    }
    catch (e: Throwable) {
      addSuppressedException(e)
    }
    finally {
      super.tearDown()
    }
  }

  override fun runInDispatchThread(): Boolean = false

  fun `test no yaml in project`() = runTest {
    val viewModel = QodanaYamlViewModelImpl(project, backgroundScope)

    val yamlState = viewModel.yamlStateFlow.filterNotNull().first()
    assertThat(yamlState).isNotNull
    assertThat(yamlState.isPhysical).isFalse
  }

  fun `test yaml in project`() = runTest {
    val viewModel = QodanaYamlViewModelImpl(project, backgroundScope)

    val yamlState = viewModel.yamlStateFlow.filterNotNull().first()
    assertThat(yamlState).isNotNull
    assertThat(yamlState.isPhysical).isTrue
    assertThat(yamlState.document.text).isEqualTo(EXPECTED_VALID_PHYSICAL_YAML_CONTENT)
  }

  fun `test yml in project`() = runTest {
    val viewModel = QodanaYamlViewModelImpl(project, backgroundScope)

    val yamlState = viewModel.yamlStateFlow.filterNotNull().first()
    assertThat(yamlState).isNotNull
    assertThat(yamlState.isPhysical).isTrue
    assertThat(yamlState.document.text).isEqualTo(EXPECTED_VALID_PHYSICAL_YAML_CONTENT)
  }

  fun `test yaml appeared in project`() = runTest {
    val viewModel = QodanaYamlViewModelImpl(project, backgroundScope)

    val yamlState = viewModel.yamlStateFlow.filterNotNull().first()
    assertThat(yamlState).isNotNull
    assertThat(yamlState.isPhysical).isFalse

    createPhysicalQodanaYaml()

    val newYamlState = viewModel.yamlStateFlow.filter { it !== yamlState }.first()
    assertThat(newYamlState).isNotNull
    assertThat(newYamlState!!.isPhysical).isTrue
    assertThat(newYamlState.document.text).isEqualTo(EXPECTED_VALID_PHYSICAL_YAML_CONTENT)
  }

  fun `test yaml disappeared in project`() = runTest {
    val viewModel = QodanaYamlViewModelImpl(project, backgroundScope)

    val yamlState = viewModel.yamlStateFlow.filterNotNull().first()
    assertThat(yamlState).isNotNull
    assertThat(yamlState.isPhysical).isTrue
    assertThat(yamlState.document.text).isEqualTo(EXPECTED_VALID_PHYSICAL_YAML_CONTENT)

    deletePhysicalQodanaYaml()

    val newYamlState = viewModel.yamlStateFlow.filter { it !== yamlState }.first()
    assertThat(newYamlState).isNotNull
    assertThat(newYamlState!!.isPhysical).isFalse
  }

  fun `test yml appeared in project`() = runTest {
    val viewModel = QodanaYamlViewModelImpl(project, backgroundScope)

    val yamlState = viewModel.yamlStateFlow.filterNotNull().first()
    assertThat(yamlState).isNotNull
    assertThat(yamlState.isPhysical).isFalse

    createPhysicalQodanaYml()

    val newYamlState = viewModel.yamlStateFlow.filter { it !== yamlState }.first()
    assertThat(newYamlState).isNotNull
    assertThat(newYamlState!!.isPhysical).isTrue
    assertThat(newYamlState.document.text).isEqualTo(EXPECTED_VALID_PHYSICAL_YAML_CONTENT)
  }

  fun `test yml disappeared in project`() = runTest {
    val viewModel = QodanaYamlViewModelImpl(project, backgroundScope)

    val yamlState = viewModel.yamlStateFlow.filterNotNull().first()
    assertThat(yamlState).isNotNull
    assertThat(yamlState.isPhysical).isTrue
    assertThat(yamlState.document.text).isEqualTo(EXPECTED_VALID_PHYSICAL_YAML_CONTENT)

    deletePhysicalQodanaYml()

    val newYamlState = viewModel.yamlStateFlow.filter { it !== yamlState }.first()
    assertThat(newYamlState).isNotNull
    assertThat(newYamlState!!.isPhysical).isFalse
  }

  fun `test parse valid in memory yaml`() = runTest {
    val viewModel = QodanaYamlViewModelImpl(project, backgroundScope)

    val yamlState = viewModel.yamlStateFlow.filterNotNull().first()
    assertThat(yamlState).isNotNull
    assertThat(yamlState.isPhysical).isFalse
    
    var error: QodanaYamlViewModel.ParseResult.Error? = null
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewModel.yamlValidationErrorFlow.collect {
        error = it
      }
    }

    val parseResult = viewModel.parseQodanaYaml().await() as QodanaYamlViewModel.ParseResult.Valid
    assertThat(parseResult.yamlConfig).isEqualTo(expectedValidYamlConfig)
    testScheduler.advanceUntilIdle()
    assertThat(error).isNull()
  }

  fun `test parse invalid in memory yaml`() = runTest {
    val viewModel = QodanaYamlViewModelImpl(project, backgroundScope)

    val yamlState = viewModel.yamlStateFlow.filterNotNull().first()
    assertThat(yamlState).isNotNull
    assertThat(yamlState.isPhysical).isFalse

    var errorFromErrorFlow: QodanaYamlViewModel.ParseResult.Error? = null
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewModel.yamlValidationErrorFlow.collect {
        errorFromErrorFlow = it
      }
    }

    WriteCommandAction.runWriteCommandAction(project) {
      yamlState.document.insertString(0, "\n invalid yaml text")
    }

    val errorFromValidation = viewModel.parseQodanaYaml().await() as QodanaYamlViewModel.ParseResult.Error
    testScheduler.advanceUntilIdle()
    assertThat(errorFromValidation).isEqualTo(errorFromErrorFlow)
  }

  fun `test parse valid physical yaml`() = runTest {
    val viewModel = QodanaYamlViewModelImpl(project, backgroundScope)

    val yamlState = viewModel.yamlStateFlow.filterNotNull().first()
    assertThat(yamlState).isNotNull
    assertThat(yamlState.isPhysical).isTrue

    var error: QodanaYamlViewModel.ParseResult.Error? = null
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewModel.yamlValidationErrorFlow.collect {
        error = it
      }
    }

    val parseResult = viewModel.parseQodanaYaml().await() as QodanaYamlViewModel.ParseResult.Valid
    assertThat(parseResult.yamlConfig).isEqualTo(expectedValidYamlConfig)
    testScheduler.advanceUntilIdle()
    assertThat(error).isNull()
  }

  fun `test parse invalid physical yaml`() = runTest {
    val viewModel = QodanaYamlViewModelImpl(project, backgroundScope)

    val yamlState = viewModel.yamlStateFlow.filterNotNull().first()
    assertThat(yamlState).isNotNull
    assertThat(yamlState.isPhysical).isTrue

    var errorFromErrorFlow: QodanaYamlViewModel.ParseResult.Error? = null
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewModel.yamlValidationErrorFlow.collect {
        errorFromErrorFlow = it
      }
    }

    val errorFromValidation = viewModel.parseQodanaYaml().await() as QodanaYamlViewModel.ParseResult.Error
    testScheduler.advanceUntilIdle()
    assertThat(errorFromValidation).isEqualTo(errorFromErrorFlow)
  }

  fun `test parse happens on viewModel scope`() = runTest {
    val viewModelScope = backgroundScope.childScope()
    val viewModel = QodanaYamlViewModelImpl(project, viewModelScope)

    val yamlState = viewModel.yamlStateFlow.filterNotNull().first()
    assertThat(yamlState).isNotNull
    assertThat(yamlState.isPhysical).isFalse

    viewModelScope.coroutineContext.job.cancelAndJoin()

    val parseJob = viewModel.parseQodanaYaml()
    assertThat(parseJob.isCancelled).isTrue
  }

  fun `test write in memory yaml`() = runTest {
    val viewModel = QodanaYamlViewModelImpl(project, backgroundScope)

    val yamlState = viewModel.yamlStateFlow.filterNotNull().first()
    assertThat(yamlState).isNotNull
    assertThat(yamlState.isPhysical).isFalse

    val writtenYamlPath = viewModel.writeQodanaYamlIfNeeded().await()

    assertThat(writtenYamlPath).isNotNull
    val physicalQodanaYamlVirtualFile = physicalQodanaYaml()!!
    assertThat(writtenYamlPath).isEqualTo(physicalQodanaYamlVirtualFile.toNioPath())
    assertThat(physicalQodanaYamlVirtualFile.readText()).isEqualTo(yamlState.document.text)
  }

  fun `test write yaml happens not on viewModel scope`() = runTest {
    val viewModelScope = backgroundScope.childScope()
    val viewModel = QodanaYamlViewModelImpl(project, viewModelScope)

    val yamlState = viewModel.yamlStateFlow.filterNotNull().first()
    assertThat(yamlState).isNotNull
    assertThat(yamlState.isPhysical).isFalse

    viewModelScope.coroutineContext.job.cancelAndJoin()
    val writtenYamlPath = viewModel.writeQodanaYamlIfNeeded().await()

    assertThat(writtenYamlPath).isNotNull
    val physicalQodanaYamlVirtualFile = physicalQodanaYaml()!!
    assertThat(writtenYamlPath).isEqualTo(physicalQodanaYamlVirtualFile.toNioPath())
    assertThat(physicalQodanaYamlVirtualFile.readText()).isEqualTo(yamlState.document.text)
  }

  private fun physicalQodanaYaml(): VirtualFile? {
    return myFixture.tempDirFixture.getFile("qodana.yaml")
  }

  private fun createPhysicalQodanaYaml() {
    myFixture.tempDirFixture.createFile("qodana.yaml", EXPECTED_VALID_PHYSICAL_YAML_CONTENT)
  }

  private fun createPhysicalQodanaYml() {
    myFixture.tempDirFixture.createFile("qodana.yml", EXPECTED_VALID_PHYSICAL_YAML_CONTENT)
  }

  private suspend fun deletePhysicalQodanaYaml() {
    writeAction {
      myFixture.tempDirFixture.getFile("qodana.yaml")!!.delete(this)
    }
  }

  private suspend fun deletePhysicalQodanaYml() {
    writeAction {
      myFixture.tempDirFixture.getFile("qodana.yml")!!.delete(this)
    }
  }
}