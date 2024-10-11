package org.jetbrains.qodana.ui.ci

import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readText
import com.intellij.openapi.vfs.writeText
import com.intellij.testFramework.EditorTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.utils.vfs.createFile
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.QodanaPluginHeavyTestBase
import org.jetbrains.qodana.assertSingleNotificationWithMessage
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.dispatchAllTasksOnUi
import org.jetbrains.qodana.report.BannerContentProvider
import org.jetbrains.qodana.runDispatchingOnUi
import org.jetbrains.qodana.ui.ProjectVcsDataProviderMock
import org.jetbrains.qodana.ui.ci.providers.CIConfigFileState
import org.jetbrains.qodana.ui.ci.providers.github.SetupGitHubActionsViewModel
import kotlin.io.path.Path
import kotlin.io.path.pathString

private const val TOKEN_TEXT = "{{ secrets.QODANA_TOKEN }}"
private const val REF_TEXT = "{{ github.event.pull_request.head.sha }}"

@TestDataPath("\$CONTENT_ROOT/test-data/SetupGitHubActionsViewModelTest")
class SetupGitHubActionsViewModelTest : QodanaPluginHeavyTestBase() {
  private val emptyProjectVcsDataProvider = ProjectVcsDataProviderMock()

  private val DEFAULT_PROJECT_STORED_EXPECTED_YML by lazy {
    @Language("YAML")
    val expected = """
      name: Qodana
      on:
        workflow_dispatch:
        pull_request:
        push:
          branches:
            - main
      
      jobs:
        qodana:
          runs-on: ubuntu-latest
          permissions:
            contents: write
            pull-requests: write
            checks: write
          steps:
            - uses: actions/checkout@v3
              with:
                ref: $$REF_TEXT
                fetch-depth: 0
            - name: 'Qodana Scan'
              uses: JetBrains/qodana-action@v42
              env:
                QODANA_TOKEN: $$TOKEN_TEXT
    """.trimIndent()
    expected
  }

  private val DEFAULT_EXPECTED_YML by lazy {
    @Language("YAML")
    val expected = """
      name: Qodana
      on:
        workflow_dispatch:
        pull_request:
        push:
          branches:
            - main
      
      jobs:
        qodana:
          runs-on: ubuntu-latest
          permissions:
            contents: write
            pull-requests: write
            checks: write
          steps:
            - uses: actions/checkout@v3
              with:
                ref: $$REF_TEXT
                fetch-depth: 0
            - name: 'Qodana Scan'
              uses: JetBrains/qodana-action@v${ApplicationInfo.getInstance().shortVersion}
              env:
                QODANA_TOKEN: $$TOKEN_TEXT
    """.trimIndent()
    expected
  }

  override fun getBasePath(): String = Path(super.getBasePath(), "SetupGitHubActionsViewModelTest").pathString

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

  fun `test no yaml in project`() = runDispatchingOnUi {
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupGitHubActionsViewModel(path, project, scope, emptyProjectVcsDataProvider)

    val configState = viewModel.configEditorStateFlow.filterNotNull().first()
    assertThat(configState).isNotNull
    assertThat(configState.ciConfigFileState).isNotNull
    assertThat(configState.ciConfigFileState).isInstanceOf(CIConfigFileState.InMemory::class.java)
  }

  fun `test yaml in project`() = runDispatchingOnUi {
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupGitHubActionsViewModel(path, project, scope, emptyProjectVcsDataProvider)

    val configState = viewModel.configEditorStateFlow.filterNotNull().first()
    assertThat(configState).isNotNull
    assertThat(configState.ciConfigFileState).isNotNull
    assertThat(configState.ciConfigFileState).isInstanceOf(CIConfigFileState.Physical::class.java)
    assertThat(configState.ciConfigFileState.document.text).isEqualTo(DEFAULT_PROJECT_STORED_EXPECTED_YML)
  }

  fun `test yaml in project without qodana section`() = runDispatchingOnUi {
    @Language("YAML")
    val expected = """
      name: Another
      on:
        workflow_dispatch:
        pull_request:
        push:
          branches:
            - main

      jobs:
        another:
          runs-on: ubuntu-latest
          steps:
            - uses: actions/checkout@v3
              with:
                fetch-depth: 0
            - name: 'Another Scan'
              uses: JetBrains/anoter-action@v2022.3.4

        qodana:
          runs-on: ubuntu-latest
          permissions:
            contents: write
            pull-requests: write
            checks: write
          steps:
            - uses: actions/checkout@v3
              with:
                ref: $$REF_TEXT
                fetch-depth: 0
            - name: 'Qodana Scan'
              uses: JetBrains/qodana-action@v${ApplicationInfo.getInstance().shortVersion}
              env:
                QODANA_TOKEN: $$TOKEN_TEXT
    """.trimIndent()
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupGitHubActionsViewModel(path, project, scope, emptyProjectVcsDataProvider)

    val configState = viewModel.configEditorStateFlow.filterNotNull().first()
    assertThat(configState).isNotNull
    assertThat(configState.ciConfigFileState).isNotNull
    assertThat(configState.ciConfigFileState).isInstanceOf(CIConfigFileState.InMemoryPatchOfPhysicalFile::class.java)
    assertThat(configState.ciConfigFileState.document.text).isEqualTo(expected)
  }

  fun `test yaml appeared in project`() = runDispatchingOnUi {
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupGitHubActionsViewModel(path, project, scope, emptyProjectVcsDataProvider)

    val configState = viewModel.configEditorStateFlow.filterNotNull().first()
    assertThat(configState).isNotNull
    assertThat(configState.ciConfigFileState).isNotNull
    assertThat(configState.ciConfigFileState).isInstanceOf(CIConfigFileState.InMemory::class.java)

    dispatchAllTasksOnUi()

    createPhysicalConfigYml()

    val newConfigState = viewModel.configEditorStateFlow.filter { it !== configState }.first()
    assertThat(newConfigState).isNotNull
    assertThat(newConfigState!!.ciConfigFileState).isNotNull
    assertThat(newConfigState.ciConfigFileState).isInstanceOf(CIConfigFileState.Physical::class.java)
    assertThat(newConfigState.ciConfigFileState.document.text).isEqualTo(DEFAULT_EXPECTED_YML)
  }

  fun `test yaml disappeared in project`() = runDispatchingOnUi {
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupGitHubActionsViewModel(path, project, scope, emptyProjectVcsDataProvider)

    val configState = viewModel.configEditorStateFlow.filterNotNull().first()
    assertThat(configState).isNotNull
    assertThat(configState.ciConfigFileState).isNotNull
    assertThat(configState.ciConfigFileState).isInstanceOf(CIConfigFileState.Physical::class.java)
    assertThat(configState.ciConfigFileState.document.text).isEqualTo(DEFAULT_PROJECT_STORED_EXPECTED_YML)

    dispatchAllTasksOnUi()

    deletePhysicalConfigYml()

    val newConfigState = viewModel.configEditorStateFlow.filter { it !== configState }.first()
    assertThat(newConfigState).isNotNull
    assertThat(newConfigState!!.ciConfigFileState).isNotNull
    assertThat(newConfigState.ciConfigFileState).isInstanceOf(CIConfigFileState.InMemory::class.java)
    assertThat(newConfigState.ciConfigFileState.document.text).isEqualTo(DEFAULT_EXPECTED_YML)
  }

  fun `test write inMemory`() = runDispatchingOnUi {
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupGitHubActionsViewModel(path, project, scope, emptyProjectVcsDataProvider)

    val configState = viewModel.configEditorStateFlow.filterNotNull().first()
    assertThat(configState).isNotNull
    assertThat(configState.ciConfigFileState).isNotNull
    assertThat(configState.ciConfigFileState).isInstanceOf(CIConfigFileState.InMemory::class.java)
    assertThat(configState.ciConfigFileState.document.text).isEqualTo(DEFAULT_EXPECTED_YML)

    val nullFile = physicalConfigYml()
    assertThat(nullFile).isNull()

    configState.ciConfigFileState.writeFile()

    val physicalConfigFile = physicalConfigYml()
    assertThat(physicalConfigFile).isNotNull
    assertThat(physicalConfigFile?.readText()).isEqualTo(DEFAULT_EXPECTED_YML)
  }

  fun `test finishProviderFlow`()  = runDispatchingOnUi {
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupGitHubActionsViewModel(path, project, scope, emptyProjectVcsDataProvider)

    val configState = viewModel.configEditorStateFlow.filterNotNull().first()
    assertThat(configState).isNotNull
    assertThat(configState.ciConfigFileState).isNotNull
    assertThat(configState.ciConfigFileState).isInstanceOf(CIConfigFileState.InMemory::class.java)
    assertThat(configState.ciConfigFileState.document.text).isEqualTo(DEFAULT_EXPECTED_YML)
    dispatchAllTasksOnUi()

    val nullFile = physicalConfigYml()
    assertThat(nullFile).isNull()

    val action = viewModel.finishProviderFlow.first()
    assertThat(action).isNotNull
    assertSingleNotificationWithMessage("Qodana will monitor code quality when changes are pushed to the remote") {
      action!!()
    }

    val physicalConfigFile = physicalConfigYml()
    assertThat(physicalConfigFile).isNotNull
    assertThat(physicalConfigFile?.readText()).isEqualTo(DEFAULT_EXPECTED_YML)
  }

  fun `test write new path inMemory`() = runDispatchingOnUi {
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupGitHubActionsViewModel(path, project, scope, emptyProjectVcsDataProvider)

    viewModel.configStringPathStateFlow.first { it.endsWith("qodana_code_quality.yml") }

    viewModel.setConfigStringPath("$path/.github/workflows/new_code_quality.yml")

    val configState = viewModel.configEditorStateFlow.filterNotNull().first { it.ciConfigFileState.absoluteNioPath?.endsWith("qodana_code_quality.yml") != true }
    assertThat(configState).isNotNull
    assertThat(configState.ciConfigFileState).isNotNull
    assertThat(configState.ciConfigFileState).isInstanceOf(CIConfigFileState.InMemory::class.java)
    assertThat(configState.ciConfigFileState.document.text).isEqualTo(DEFAULT_EXPECTED_YML)

    val nullFile = physicalConfigYml()
    assertThat(nullFile).isNull()

    configState.ciConfigFileState.writeFile()

    val physicalConfigFile = physicalConfigYml("new_code_quality.yml")
    assertThat(physicalConfigFile).isNotNull
    assertThat(physicalConfigFile?.readText()).isEqualTo(DEFAULT_EXPECTED_YML)
  }

  fun `test set invalid path`() = runDispatchingOnUi {
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupGitHubActionsViewModel(path, project, scope, emptyProjectVcsDataProvider)

    viewModel.configStringPathErrorMessageFlow.first { it == null }

    viewModel.setConfigStringPath("")

    val errorEmpty = viewModel.configStringPathErrorMessageFlow.filterNotNull().first()
    assertThat(errorEmpty).isNotNull
    assertThat(errorEmpty).isEqualTo("Workflow file location can't be empty")

    viewModel.setConfigStringPath("not/absolute/.github/workflows/config.yml")

    val errorNotAbsolute = viewModel.configStringPathErrorMessageFlow.first { it !== errorEmpty }
    assertThat(errorNotAbsolute).isNotNull
    assertThat(errorNotAbsolute).isEqualTo("Path must be absolute")

    viewModel.setConfigStringPath("$path/workflows/new_code_quality.yml")

    val errorWrongLocation = viewModel.configStringPathErrorMessageFlow.first { it !== errorNotAbsolute }
    assertThat(errorWrongLocation).isNotNull
    assertThat(errorWrongLocation).isEqualTo("Workflow file must be located in '.github/workflows' project directory")

    viewModel.setConfigStringPath("$path/.github/workflows/new_code_quality.json")

    val errorNotYaml = viewModel.configStringPathErrorMessageFlow.first { it !== errorWrongLocation }
    assertThat(errorNotYaml).isNotNull
    assertThat(errorNotYaml).isEqualTo("Workflow must be a YAML file")
  }

  fun `test update inMemoryPatch config`() = runDispatchingOnUi {
    @Language("YAML")
    val expected = """
      name: Another
      on:
        workflow_dispatch:
        pull_request:
        push:
          branches:
            - main

      jobs:
        another:
          runs-on: ubuntu-latest
          steps:
            - uses: actions/checkout@v3
              with:
                fetch-depth: 0
            - name: 'Another Scan'
              uses: JetBrains/anoter-action@v42

        qodana:
          runs-on: ubuntu-latest
          permissions:
            contents: write
            pull-requests: write
            checks: write
          steps:
            - uses: actions/checkout@v3
              with:
                ref: $$REF_TEXT
                fetch-depth: 0
            - name: 'Qodana Scan'
              uses: JetBrains/qodana-action@v${ApplicationInfo.getInstance().shortVersion}
              env:
                QODANA_TOKEN: $$TOKEN_TEXT
    """.trimIndent()
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupGitHubActionsViewModel(path, project, scope, emptyProjectVcsDataProvider)

    val configState = viewModel.configEditorStateFlow.filterNotNull().first()
    assertThat(configState).isNotNull
    assertThat(configState.ciConfigFileState).isNotNull
    assertThat(configState.ciConfigFileState).isInstanceOf(CIConfigFileState.InMemoryPatchOfPhysicalFile::class.java)
    val initialText = physicalConfigYml()!!.readText()
    assertThat(configState.ciConfigFileState.document.text).isNotEqualTo(initialText)

    configState.ciConfigFileState.writeFile()
    dispatchAllTasksOnUi()

    val physicalConfigFile = physicalConfigYml()
    assertThat(physicalConfigFile).isNotNull
    assertThat(physicalConfigFile?.readText()).isEqualTo(expected)
  }

  fun `test yaml with another name in project`() = runDispatchingOnUi {
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupGitHubActionsViewModel(path, project, scope, emptyProjectVcsDataProvider)

    val configState = viewModel.configEditorStateFlow.filterNotNull().first()
    assertThat(configState).isNotNull
    assertThat(configState.ciConfigFileState).isNotNull
    assertThat(configState.ciConfigFileState.absoluteNioPath?.fileName?.pathString).isNotEqualTo("qodana_code_quality.yml")
    assertThat(configState.ciConfigFileState).isInstanceOf(CIConfigFileState.Physical::class.java)
    assertThat(configState.ciConfigFileState.document.text).isEqualTo(DEFAULT_PROJECT_STORED_EXPECTED_YML)
  }

  fun `test setting git branches`() = runDispatchingOnUi {
    @Language("YAML")
    val expected = """
      name: Qodana
      on:
        workflow_dispatch:
        pull_request:
        push:
          branches:
            - main
            - dev
            - test-branch
      
      jobs:
        qodana:
          runs-on: ubuntu-latest
          permissions:
            contents: write
            pull-requests: write
            checks: write
          steps:
            - uses: actions/checkout@v3
              with:
                ref: $$REF_TEXT
                fetch-depth: 0
            - name: 'Qodana Scan'
              uses: JetBrains/qodana-action@v${ApplicationInfo.getInstance().shortVersion}
              env:
                QODANA_TOKEN: $$TOKEN_TEXT
    """.trimIndent()

    val projectVcsDataProviderWithBranches = ProjectVcsDataProviderMock(
      projectBranches = listOf("main", "dev", "another"),
      currentBranch = "test-branch"
    )
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupGitHubActionsViewModel(path, project, scope, projectVcsDataProviderWithBranches)

    val configState = viewModel.configEditorStateFlow.filterNotNull().first()
    assertThat(configState).isNotNull
    assertThat(configState.ciConfigFileState).isNotNull
    assertThat(configState.ciConfigFileState).isInstanceOf(CIConfigFileState.InMemory::class.java)

    assertThat(configState.ciConfigFileState.document.text).isEqualTo(expected)
  }

  fun `test sarif baseline in project`() = runDispatchingOnUi {
    @Language("YAML")
    val expected = """
      name: Qodana
      on:
        workflow_dispatch:
        pull_request:
        push:
          branches:
            - main
      
      jobs:
        qodana:
          runs-on: ubuntu-latest
          permissions:
            contents: write
            pull-requests: write
            checks: write
          steps:
            - uses: actions/checkout@v3
              with:
                ref: $$REF_TEXT
                fetch-depth: 0
            - name: 'Qodana Scan'
              uses: JetBrains/qodana-action@v${ApplicationInfo.getInstance().shortVersion}
              env:
                QODANA_TOKEN: $$TOKEN_TEXT
              with:
                args: --baseline,qodana.sarif.json
    """.trimIndent()

    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupGitHubActionsViewModel(path, project, scope, emptyProjectVcsDataProvider)

    val configState = viewModel.configEditorStateFlow.filterNotNull().first()
    assertThat(configState).isNotNull
    assertThat(configState.ciConfigFileState).isNotNull
    assertThat(configState.ciConfigFileState).isInstanceOf(CIConfigFileState.InMemory::class.java)

    assertThat(configState.ciConfigFileState.document.text).isEqualTo(expected)
  }

  fun `test banner visible`() = runDispatchingOnUi {
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupGitHubActionsViewModel(path, project, scope, emptyProjectVcsDataProvider)
    dispatchAllTasksOnUi()

    var banner: BannerContentProvider? = null

    scope.launch(QodanaDispatchers.Default) {
      viewModel.bannerContentProviderFlow.collect {
        banner = it
      }
    }

    dispatchAllTasksOnUi()
    assertThat(banner).isNotNull
  }

  private fun physicalConfigYml(filename: String = "qodana_code_quality.yml"): VirtualFile? {
    return myFixture.tempDirFixture.getFile(".github/workflows/$filename")
  }

  private suspend fun createPhysicalConfigYml(filename: String = "qodana_code_quality.yml") {
    writeAction {
      val createdFile = myFixture.tempDirFixture.findOrCreateDir(".github/workflows").createFile(filename)
      createdFile.writeText(DEFAULT_EXPECTED_YML)
    }
  }

  private suspend fun deletePhysicalConfigYml() {
    writeAction {
      myFixture.tempDirFixture.getFile(".github/workflows/qodana_code_quality.yml")!!.delete(this)
    }
  }
}