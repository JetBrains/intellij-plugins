package org.jetbrains.qodana.jvm.groovy

import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readText
import com.intellij.testFramework.EditorTestUtil
import com.intellij.testFramework.TestDataPath
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import org.assertj.core.api.Assertions
import org.jetbrains.qodana.QodanaPluginHeavyTestBase
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

  fun `test config file in project without qodana section`() = runDispatchingOnUi {
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupJenkinsViewModel(path, project, scope, emptyProjectVcsDataProvider)

    val configState = viewModel.baseSetupCIViewModel.configEditorStateFlow.filterNotNull().first()
    Assertions.assertThat(configState).isNotNull
    Assertions.assertThat(configState.ciConfigFileState).isNotNull
    Assertions.assertThat(configState.ciConfigFileState).isInstanceOf(CIConfigFileState.InMemoryPatchOfPhysicalFile::class.java)
    Assertions.assertThat(configState.ciConfigFileState.document.text).isEqualTo(expectedText)
  }

  fun `test update inMemoryPatch config`() = runDispatchingOnUi {
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupJenkinsViewModel(path, project, scope, emptyProjectVcsDataProvider)

    val configState = viewModel.baseSetupCIViewModel.configEditorStateFlow.filterNotNull().first()
    Assertions.assertThat(configState).isNotNull
    Assertions.assertThat(configState.ciConfigFileState).isNotNull
    Assertions.assertThat(configState.ciConfigFileState).isInstanceOf(CIConfigFileState.InMemoryPatchOfPhysicalFile::class.java)

    configState.ciConfigFileState.writeFile()

    val physicalConfigFile = physicalConfigFile()
    Assertions.assertThat(physicalConfigFile).isNotNull
    Assertions.assertThat(physicalConfigFile?.readText()).isEqualTo(expectedText)
  }

  private val expectedText: String
    get() = myFixture.tempDirFixture.getFile("expected")?.readText() ?: ""

  private fun physicalConfigFile(): VirtualFile? {
    return myFixture.tempDirFixture.getFile("Jenkinsfile")
  }
}