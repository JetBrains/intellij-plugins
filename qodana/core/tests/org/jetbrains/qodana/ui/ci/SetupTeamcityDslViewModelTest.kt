package org.jetbrains.qodana.ui.ci

import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.openapi.vfs.readText
import com.intellij.testFramework.EditorTestUtil
import com.intellij.testFramework.TestDataPath
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.QodanaPluginHeavyTestBase
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.dispatchAllTasksOnUi
import org.jetbrains.qodana.report.BannerContentProvider
import org.jetbrains.qodana.runDispatchingOnUi
import org.jetbrains.qodana.ui.ProjectVcsDataProviderMock
import org.jetbrains.qodana.ui.ci.providers.teamcity.SetupTeamcityDslViewModel
import kotlin.io.path.Path
import kotlin.io.path.pathString

@TestDataPath("\$CONTENT_ROOT/test-data/SetupTeamcityDslViewModelTest")
class SetupTeamcityDslViewModelTest : QodanaPluginHeavyTestBase() {
  override fun getBasePath(): String = Path(super.getBasePath(), "SetupTeamcityDslViewModelTest").pathString

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

  fun `test simple snippet`() = runDispatchingOnUi {
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupTeamcityDslViewModel(path, project, scope, ProjectVcsDataProviderMock())

    val editor = viewModel.configEditorDeferred.await()

    assertThat(editor.document.text).isEqualTo(expectedText)
  }

  fun `test right vcs provider`() = runDispatchingOnUi {
    val projectVcsDataProviderMock = ProjectVcsDataProviderMock(
      originUrl = "https://test-git.com/test-project.git",
      projectBranches = listOf("main", "dev", "another"),
      currentBranch = "test-branch"
    )
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupTeamcityDslViewModel(path, project, scope, projectVcsDataProviderMock)

    val editor = viewModel.configEditorDeferred.await()

    assertThat(editor.document.text).isEqualTo(expectedText)
  }

  fun `test sarif baseline in project`() = runDispatchingOnUi {
    val projectVcsDataProviderMock = ProjectVcsDataProviderMock(
      originUrl = "https://test-git.com/test-project.git",
      projectBranches = listOf("main", "dev", "another"),
      currentBranch = "test-branch"
    )
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupTeamcityDslViewModel(path, project, scope, projectVcsDataProviderMock)

    val editor = viewModel.configEditorDeferred.await()

    assertThat(editor.document.text).isEqualTo(expectedText)
  }

  fun `test master branch in vcs provider`() = runDispatchingOnUi {
    val projectVcsDataProviderMock = ProjectVcsDataProviderMock(
      originUrl = "https://test-git.com/test-project.git",
      projectBranches = listOf("dev", "release", "another"),
      currentBranch = "master"
    )
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupTeamcityDslViewModel(path, project, scope, projectVcsDataProviderMock)

    val editor = viewModel.configEditorDeferred.await()

    assertThat(editor.document.text).isEqualTo(expectedText)
  }

  fun `test banner visible`() = runDispatchingOnUi {
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupTeamcityDslViewModel(path, project, scope, ProjectVcsDataProviderMock())

    var banner: BannerContentProvider? = null

    scope.launch(QodanaDispatchers.Default) {
      viewModel.bannerContentProviderFlow.collect {
        banner = it
      }
    }

    dispatchAllTasksOnUi()
    assertThat(banner).isNotNull
  }


  private val expectedText: String
    get() = myFixture.tempDirFixture.getFile("expected.kts")?.readText() ?: ""
}