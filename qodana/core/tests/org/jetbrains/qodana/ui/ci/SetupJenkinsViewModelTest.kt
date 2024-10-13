package org.jetbrains.qodana.ui.ci

import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readText
import com.intellij.testFramework.EditorTestUtil
import com.intellij.testFramework.ExtensionTestUtil
import com.intellij.testFramework.TestDataPath
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.assertj.core.api.Assertions
import org.jetbrains.qodana.QodanaPluginHeavyTestBase
import org.jetbrains.qodana.assertSingleNotificationWithMessage
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.dispatchAllTasksOnUi
import org.jetbrains.qodana.extensions.ci.JenkinsConfigHandler
import org.jetbrains.qodana.report.BannerContentProvider
import org.jetbrains.qodana.runDispatchingOnUi
import org.jetbrains.qodana.ui.ProjectVcsDataProviderMock
import org.jetbrains.qodana.ui.ci.providers.CIConfigFileState
import org.jetbrains.qodana.ui.ci.providers.jenkins.SetupJenkinsViewModel
import kotlin.io.path.Path
import kotlin.io.path.pathString

@TestDataPath("\$CONTENT_ROOT/test-data/SetupJenkinsViewModelTest")
class SetupJenkinsViewModelTest : QodanaPluginHeavyTestBase() {
  private val emptyProjectVcsDataProvider = ProjectVcsDataProviderMock()

  override fun getBasePath(): String = Path(super.getBasePath(), "SetupJenkinsViewModelTest").pathString

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
      scope.cancel()
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

  fun `test no config file in project`() = runDispatchingOnUi {
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupJenkinsViewModel(path, project, scope, emptyProjectVcsDataProvider)

    val configState = viewModel.baseSetupCIViewModel.configEditorStateFlow.filterNotNull().first()
    Assertions.assertThat(configState).isNotNull
    Assertions.assertThat(configState.ciConfigFileState).isNotNull
    Assertions.assertThat(configState.ciConfigFileState).isInstanceOf(CIConfigFileState.InMemory::class.java)
  }

  fun `test config file in project`() = runDispatchingOnUi {
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupJenkinsViewModel(path, project, scope, emptyProjectVcsDataProvider)

    val configState = viewModel.baseSetupCIViewModel.configEditorStateFlow.filterNotNull().first()
    Assertions.assertThat(configState).isNotNull
    Assertions.assertThat(configState.ciConfigFileState).isNotNull
    Assertions.assertThat(configState.ciConfigFileState).isInstanceOf(CIConfigFileState.Physical::class.java)
    Assertions.assertThat(configState.ciConfigFileState.document.text).isEqualTo(expectedText)
  }

  fun `test config file appeared in project`() = runDispatchingOnUi {
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupJenkinsViewModel(path, project, scope, emptyProjectVcsDataProvider)

    val configState = viewModel.baseSetupCIViewModel.configEditorStateFlow.filterNotNull().first()
    Assertions.assertThat(configState).isNotNull
    Assertions.assertThat(configState.ciConfigFileState).isNotNull
    Assertions.assertThat(configState.ciConfigFileState).isInstanceOf(CIConfigFileState.InMemory::class.java)
    dispatchAllTasksOnUi()

    createPhysicalConfigFile()

    val newConfigState = viewModel.baseSetupCIViewModel.configEditorStateFlow.filter { it !== configState }.first()
    Assertions.assertThat(newConfigState).isNotNull
    Assertions.assertThat(newConfigState!!.ciConfigFileState).isNotNull
    Assertions.assertThat(newConfigState.ciConfigFileState).isInstanceOf(CIConfigFileState.Physical::class.java)
    Assertions.assertThat(newConfigState.ciConfigFileState.document.text).isEqualTo(expectedText)
  }

  fun `test config file disappeared in project`() = runDispatchingOnUi {
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupJenkinsViewModel(path, project, scope, emptyProjectVcsDataProvider)

    val configState = viewModel.baseSetupCIViewModel.configEditorStateFlow.filterNotNull().first()
    Assertions.assertThat(configState).isNotNull
    Assertions.assertThat(configState.ciConfigFileState).isNotNull
    Assertions.assertThat(configState.ciConfigFileState).isInstanceOf(CIConfigFileState.Physical::class.java)
    Assertions.assertThat(configState.ciConfigFileState.document.text).isEqualTo(expectedText)

    dispatchAllTasksOnUi()

    deletePhysicalConfigFile()

    val newConfigState = viewModel.baseSetupCIViewModel.configEditorStateFlow.filter { it !== configState }.first()
    Assertions.assertThat(newConfigState).isNotNull
    Assertions.assertThat(newConfigState!!.ciConfigFileState).isNotNull
    Assertions.assertThat(newConfigState.ciConfigFileState).isInstanceOf(CIConfigFileState.InMemory::class.java)
  }

  fun `test write inMemory`() = runDispatchingOnUi {
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupJenkinsViewModel(path, project, scope, emptyProjectVcsDataProvider)

    val configState = viewModel.baseSetupCIViewModel.configEditorStateFlow.filterNotNull().first()
    Assertions.assertThat(configState).isNotNull
    Assertions.assertThat(configState.ciConfigFileState).isNotNull
    Assertions.assertThat(configState.ciConfigFileState).isInstanceOf(CIConfigFileState.InMemory::class.java)
    Assertions.assertThat(configState.ciConfigFileState.document.text).isEqualTo(expectedText)

    val nullFile = physicalConfigFile()
    Assertions.assertThat(nullFile).isNull()

    configState.ciConfigFileState.writeFile()

    val physicalConfigFile = physicalConfigFile()
    Assertions.assertThat(physicalConfigFile).isNotNull
    Assertions.assertThat(physicalConfigFile?.readText()).isEqualTo(expectedText)
  }

  fun `test finishProviderFlow`() = runDispatchingOnUi {
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupJenkinsViewModel(path, project, scope, emptyProjectVcsDataProvider)

    val configState = viewModel.baseSetupCIViewModel.configEditorStateFlow.filterNotNull().first()
    Assertions.assertThat(configState).isNotNull
    Assertions.assertThat(configState.ciConfigFileState).isNotNull
    Assertions.assertThat(configState.ciConfigFileState).isInstanceOf(CIConfigFileState.InMemory::class.java)
    Assertions.assertThat(configState.ciConfigFileState.document.text).isEqualTo(expectedText)

    val nullFile = physicalConfigFile()
    Assertions.assertThat(nullFile).isNull()

    val action = viewModel.finishProviderFlow.first()
    Assertions.assertThat(action).isNotNull

    assertSingleNotificationWithMessage("Qodana will monitor code quality when changes are pushed to the remote") {
      action!!()
    }

    val physicalConfigFile = physicalConfigFile()
    Assertions.assertThat(physicalConfigFile).isNotNull
    Assertions.assertThat(physicalConfigFile?.readText()).isEqualTo(expectedText)
  }

  fun `test setting git branches`() = runDispatchingOnUi {
    val projectVcsDataProviderWithBranches = ProjectVcsDataProviderMock(
      projectBranches = listOf("main", "dev", "another"),
      currentBranch = "test-branch"
    )
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupJenkinsViewModel(path, project, scope, projectVcsDataProviderWithBranches)

    val configState = viewModel.baseSetupCIViewModel.configEditorStateFlow.filterNotNull().first()
    Assertions.assertThat(configState).isNotNull
    Assertions.assertThat(configState.ciConfigFileState).isNotNull
    Assertions.assertThat(configState.ciConfigFileState).isInstanceOf(CIConfigFileState.InMemory::class.java)

    Assertions.assertThat(configState.ciConfigFileState.document.text).isEqualTo(expectedText)
  }

  fun `test sarif baseline in project`() = runDispatchingOnUi {
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupJenkinsViewModel(path, project, scope, emptyProjectVcsDataProvider)

    val configState = viewModel.baseSetupCIViewModel.configEditorStateFlow.filterNotNull().first()
    Assertions.assertThat(configState).isNotNull
    Assertions.assertThat(configState.ciConfigFileState).isNotNull
    Assertions.assertThat(configState.ciConfigFileState).isInstanceOf(CIConfigFileState.InMemory::class.java)

    Assertions.assertThat(configState.ciConfigFileState.document.text).isEqualTo(expectedText)
  }

  fun `test banner visible`() = runDispatchingOnUi {
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupJenkinsViewModel(path, project, scope, emptyProjectVcsDataProvider)
    dispatchAllTasksOnUi()

    var banner: BannerContentProvider? = null

    scope.launch(QodanaDispatchers.Default) {
      viewModel.baseSetupCIViewModel.bannerContentProviderFlow.collect {
        banner = it
      }
    }

    dispatchAllTasksOnUi()
    Assertions.assertThat(banner).isNotNull
  }

  fun `test dummy insert`() = runDispatchingOnUi {
    ExtensionTestUtil.maskExtensions(JenkinsConfigHandler.EP_NAME, listOf(), testRootDisposable)
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupJenkinsViewModel(path, project, scope, emptyProjectVcsDataProvider)

    val configState = viewModel.baseSetupCIViewModel.configEditorStateFlow.filterNotNull().first()
    Assertions.assertThat(configState).isNotNull
    Assertions.assertThat(configState.ciConfigFileState).isNotNull
    Assertions.assertThat(configState.ciConfigFileState).isInstanceOf(CIConfigFileState.InMemoryPatchOfPhysicalFile::class.java)
    Assertions.assertThat(configState.ciConfigFileState.document.text).isEqualTo(expectedText)
  }

  fun `test dummy insert no stages`() = runDispatchingOnUi {
    ExtensionTestUtil.maskExtensions(JenkinsConfigHandler.EP_NAME, listOf(), testRootDisposable)
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupJenkinsViewModel(path, project, scope, emptyProjectVcsDataProvider)

    val configState = viewModel.baseSetupCIViewModel.configEditorStateFlow.filterNotNull().first()
    Assertions.assertThat(configState).isNotNull
    Assertions.assertThat(configState.ciConfigFileState).isNotNull
    Assertions.assertThat(configState.ciConfigFileState).isInstanceOf(CIConfigFileState.InMemoryPatchOfPhysicalFile::class.java)
    Assertions.assertThat(configState.ciConfigFileState.document.text).isEqualTo(expectedText)
  }

  fun `test dummy insert no pipeline`() = runDispatchingOnUi {
    ExtensionTestUtil.maskExtensions(JenkinsConfigHandler.EP_NAME, listOf(), testRootDisposable)
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupJenkinsViewModel(path, project, scope, emptyProjectVcsDataProvider)

    val configState = viewModel.baseSetupCIViewModel.configEditorStateFlow.filterNotNull().first()
    Assertions.assertThat(configState).isNotNull
    Assertions.assertThat(configState.ciConfigFileState).isNotNull
    Assertions.assertThat(configState.ciConfigFileState).isInstanceOf(CIConfigFileState.InMemoryPatchOfPhysicalFile::class.java)
    Assertions.assertThat(configState.ciConfigFileState.document.text).isEqualTo(expectedText)
  }

  fun `test dummy insert different indents`() = runDispatchingOnUi {
    ExtensionTestUtil.maskExtensions(JenkinsConfigHandler.EP_NAME, listOf(), testRootDisposable)
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupJenkinsViewModel(path, project, scope, emptyProjectVcsDataProvider)

    val configState = viewModel.baseSetupCIViewModel.configEditorStateFlow.filterNotNull().first()
    Assertions.assertThat(configState).isNotNull
    Assertions.assertThat(configState.ciConfigFileState).isNotNull
    Assertions.assertThat(configState.ciConfigFileState).isInstanceOf(CIConfigFileState.InMemoryPatchOfPhysicalFile::class.java)
    Assertions.assertThat(configState.ciConfigFileState.document.text).isEqualTo(expectedText)
  }

  private val expectedText: String
    get() = myFixture.tempDirFixture.getFile("expected")?.readText() ?: ""

  private fun physicalConfigFile(): VirtualFile? {
    return myFixture.tempDirFixture.getFile("Jenkinsfile")
  }

  private suspend fun createPhysicalConfigFile() {
    writeAction {
      myFixture.tempDirFixture.createFile("Jenkinsfile", expectedText)
    }
  }

  private suspend fun deletePhysicalConfigFile() {
    writeAction {
      myFixture.tempDirFixture.getFile("Jenkinsfile")!!.delete(this)
    }
  }
}