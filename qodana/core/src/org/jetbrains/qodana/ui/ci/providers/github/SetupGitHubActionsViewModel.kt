package org.jetbrains.qodana.ui.ci.providers.github

import com.intellij.codeInsight.daemon.impl.IntentionsUI
import com.intellij.ide.BrowserUtil
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.readAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findDirectory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.cloud.openBrowserWithCurrentQodanaCloudFrontend
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.UiAnyModality
import org.jetbrains.qodana.getFileTypeByFilename
import org.jetbrains.qodana.notifications.QodanaNotifications
import org.jetbrains.qodana.report.BannerContentProvider
import org.jetbrains.qodana.ui.*
import org.jetbrains.qodana.ui.ci.SetupCIFinishProvider
import org.jetbrains.qodana.ui.ci.SetupCIViewModel
import org.jetbrains.qodana.ui.ci.providers.*
import java.nio.file.InvalidPathException
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class SetupGitHubActionsViewModel(
  private val projectNioPath: Path,
  val project: Project,
  private val scope: CoroutineScope,
  private val projectVcsDataProvider: ProjectVcsDataProvider,
) : SetupCIViewModel {
  private val _configStringPathStateFlow = MutableStateFlow("")
  val configStringPathStateFlow: StateFlow<String> = _configStringPathStateFlow.asStateFlow()

  val configStringPathErrorMessageFlow: SharedFlow<@NlsContexts.DialogMessage String?> = createConfigStringPathErrorMessageFlow()

  val configEditorStateFlow: StateFlow<CIConfigEditorState?> = createConfigEditorStateFlow()

  private val isBannerVisibleStateFlow = MutableStateFlow(true)
  val bannerContentProviderFlow: Flow<BannerContentProvider?> = isBannerVisibleStateFlow.map { if (it) createBannerContentProvider() else null }

  override val finishProviderFlow: Flow<SetupCIFinishProvider?> = createFinishProviderFlow()

  private val configFileType: FileType = getFileTypeByFilename(DEFAULT_GITHUB_WORKFLOW_FILENAME)

  init {
    scope.launch(QodanaDispatchers.Default) {
      updateConfigStringPathOnEditorStateChanges()
    }
  }

  fun setConfigStringPath(newPath: String) {
    _configStringPathStateFlow.value = newPath
  }

  override fun unselected() {
    val editor = configEditorStateFlow.value?.editor ?: return
    scope.launch(QodanaDispatchers.Ui) {
      IntentionsUI.getInstance(project).hideForEditor(editor)
    }
  }

  private fun createConfigEditorStateFlow(): StateFlow<CIConfigEditorState?> {
    val configEditorStateFlow = MutableStateFlow<CIConfigEditorState?>(null)
    scope.launch(QodanaDispatchers.Default) {
      updateConfigEditorStateOnRefreshRequests(configEditorStateFlow)
    }
    return configEditorStateFlow.asStateFlow()
  }

  private fun createConfigStringPathErrorMessageFlow(): SharedFlow<@NlsContexts.DialogMessage String?> {
    return _configStringPathStateFlow
      .map { validatePath(it) }
      .distinctUntilChanged()
      .flowOn(QodanaDispatchers.Default)
      .shareIn(scope, SharingStarted.Eagerly, replay = 1)
  }

  private fun validatePath(path: String): @NlsContexts.DialogMessage String? {
    if (path.isEmpty()) return QodanaBundle.message("gh.workflow.file.location.can.t.be.empty")

    val absolutePath = try {
      Path(path)
    }
    catch (_ : InvalidPathException) {
      return QodanaBundle.message("gh.workflow.value.not.path")
    }
    return when {
      !absolutePath.isAbsolute -> QodanaBundle.message("gh.workflow.path.must.be.absolute")
      !isInConfigDir(absolutePath) -> QodanaBundle.message("gh.workflow.file.must.be.located.in.project.directory", GITHUB_WORKFLOWS_DIR)
      !isConfigFileExtension(absolutePath) -> QodanaBundle.message("gh.workflow.must.be.yaml.file")
      else -> null
    }
  }

  private fun createFinishProviderFlow(): Flow<SetupCIFinishProvider?> {
    return flow {
      configEditorStateFlow.filterNotNull().first()
      emitAll(
        configStringPathErrorMessageFlow.map { errorMessage ->
          if (errorMessage != null) return@map null
          return@map {
            firstConfigEditorStateWithValidPath().ciConfigFileState.writeFile()
            spawnAddedConfigurationNotification()
          }
        }
      )
    }
  }

  private fun spawnAddedConfigurationNotification() {
    QodanaNotifications.General.notification(
      QodanaBundle.message("qodana.add.to.ci.finish.notification.github.title"),
      QodanaBundle.message("qodana.add.to.ci.finish.notification.text"),
      NotificationType.INFORMATION,
      withQodanaIcon = true
    ).notify(project)
  }

  private suspend fun updateConfigStringPathOnEditorStateChanges() {
    configEditorStateFlow.filterNotNull().map { it.ciConfigFileState.stringPath }.collect {
      _configStringPathStateFlow.value = it
    }
  }

  private suspend fun updateConfigEditorStateOnRefreshRequests(configEditorStateFlow: MutableStateFlow<CIConfigEditorState?>) {
    val editorsToRelease = mutableSetOf<Editor>()
    try {
      createRefreshConfigRequestsFlow(configEditorStateFlow).collectLatest { refreshRequest ->
        val newEditorState = when(refreshRequest) {
          CIConfigFileState.RefreshRequest.FindFile -> {
            processFindFileRequest()
          }
          is CIConfigFileState.RefreshRequest.SetFile -> {
            val currentEditorState = configEditorStateFlow.value ?: return@collectLatest
            val pathNotChanged = currentEditorState.ciConfigFileState.stringPath == refreshRequest.newStringPath
            if (pathNotChanged && refreshRequest.ignoreIfPathNotChanged) return@collectLatest

            processSetPathRequest(currentEditorState, refreshRequest.newStringPath)
          }
        }
        configEditorStateFlow.value = newEditorState
        editorsToRelease.add(newEditorState.editor)
      }
    }
    finally {
      withContext(QodanaDispatchers.UiAnyModality + NonCancellable) {
        editorsToRelease.filterNot { it.isDisposed }.forEach {
          EditorFactory.getInstance().releaseEditor(it)
        }
      }
    }
  }

  private fun createRefreshConfigRequestsFlow(configEditorStateFlow: StateFlow<CIConfigEditorState?>): Flow<CIConfigFileState.RefreshRequest> {
    return merge(
      flow {
        configEditorStateFlow.filterNotNull().first()
        emitAll(
          configStringPathStateFlow
            .debounce(1.5.seconds)
            .distinctUntilChanged().map {
              CIConfigFileState.RefreshRequest.SetFile(it, ignoreIfPathNotChanged = true)
            }
        )
      },
      configEditorStateFlow.flatMapLatest {
        it?.ciConfigFileState?.refreshConfigFileRequestFlow ?: flowOf(CIConfigFileState.RefreshRequest.FindFile)
      }
    )
  }

  private suspend fun processSetPathRequest(currentEditorState: CIConfigEditorState, newStringPath: String): CIConfigEditorState {
    val currentConfigState = currentEditorState.ciConfigFileState

    val newNioPath = newStringPath.toNioPathSafe()
    val isNewPathConfig = newNioPath != null && isConfigPath(newNioPath)
    val newVirtualFile = readAction { newNioPath?.let { LocalFileSystem.getInstance().findFileByNioFile(newNioPath) } }

    if (isNewPathConfig && newVirtualFile != null) {
      val newConfigState = if (newVirtualFile.alreadyContainsQodana()) {
        getPhysicalConfigState(project, newVirtualFile)
      }
      else {
        getInMemoryPatchOfPhysicalConfig(newVirtualFile)
      }

      if (newConfigState != null) {
        val editor = createEditor(project, newConfigState.document, configFileType)
        return CIConfigEditorState(editor, newConfigState)
      }
    }

    return when(currentConfigState) {
      is CIConfigFileState.InMemory -> {
        val newInMemoryState = CIConfigFileState.InMemory(project, currentConfigState.document, newStringPath)
        CIConfigEditorState(currentEditorState.editor, newInMemoryState)
      }
      else -> {
        val newInMemoryDocument = createInMemoryDocument(project, defaultConfigurationText(), DEFAULT_GITHUB_WORKFLOW_FILENAME)
        val newEditor = createEditor(project, newInMemoryDocument, configFileType)
        val newInMemoryState = CIConfigFileState.InMemory(project, newInMemoryDocument, newStringPath)
        CIConfigEditorState(newEditor, newInMemoryState)
      }
    }
  }

  private suspend fun processFindFileRequest(): CIConfigEditorState {
    val newConfigState = refreshConfigState()
    val editor = createEditor(project, newConfigState.document, configFileType)
    return CIConfigEditorState(editor, newConfigState)
  }

  private suspend fun refreshConfigState(): CIConfigFileState {
    val githubWorkflowFiles = getGithubWorkflowFiles()

    val physicalConfigState = firstConfigWithQodana(githubWorkflowFiles)?.let { getPhysicalConfigState(project, it) }
    if (physicalConfigState != null) {
      return physicalConfigState
    }

    val qodanaWorkflowFilename = "qodana_code_quality"

    val qodanaWorkflowFile = githubWorkflowFiles.firstOrNull { it.name.startsWith(qodanaWorkflowFilename) }
    val inMemoryWorkflowPatch = qodanaWorkflowFile?.let { getInMemoryPatchOfPhysicalConfig(qodanaWorkflowFile) }
    if (inMemoryWorkflowPatch != null) {
      return inMemoryWorkflowPatch
    }

    val inMemoryDocument = createInMemoryDocument(project, defaultConfigurationText(), DEFAULT_GITHUB_WORKFLOW_FILENAME)
    val absoluteStringPath = projectNioPath.resolve(GITHUB_WORKFLOWS_DIR).resolve(DEFAULT_GITHUB_WORKFLOW_FILENAME).toString()
    return CIConfigFileState.InMemory(project, inMemoryDocument, absoluteStringPath)
  }

  private suspend fun getGithubWorkflowFiles(): List<VirtualFile> {
    val workflowDir = readAction { project.guessProjectDir()?.findDirectory(GITHUB_WORKFLOWS_DIR) } ?: return emptyList()
    return workflowDir.children.filter { it.extension in GITHUB_WORKFLOWS_EXTENSIONS }
  }

  private suspend fun firstConfigWithQodana(configFiles: List<VirtualFile>): VirtualFile? {
    return configFiles.map { virtualFile ->
      flow {
        if (virtualFile.alreadyContainsQodana()) emit(virtualFile)
      }
    }.merge().firstOrNull()
  }

  private suspend fun getInMemoryPatchOfPhysicalConfig(qodanaConfigFile: VirtualFile): CIConfigFileState.InMemoryPatchOfPhysicalFile? {
    val physicalDocument = readAction { FileDocumentManager.getInstance().getDocument(qodanaConfigFile) } ?: return null
    val physicalText = physicalDocument.immutableCharSequence
    val contentOfInMemoryPatchDocument = listOf(physicalText, defaultQodanaJobText()).joinToString("\n\n")
    val inMemoryPatchDocument = createInMemoryDocument(project, contentOfInMemoryPatchDocument, qodanaConfigFile.name)

    return CIConfigFileState.InMemoryPatchOfPhysicalFile(project, inMemoryPatchDocument, physicalDocument, qodanaConfigFile.toNioPath())
  }

  private suspend fun defaultQodanaJobText(): String {
    val cloudTokenText = "\${{ secrets.QODANA_TOKEN }}"
    val refsText = "\${{ github.event.pull_request.head.sha }}"

    val baselineText = getSarifBaseline(project)?.let { "args: --baseline,$it" }

    val qodanaGitHubActionVersion = ApplicationInfo.getInstance().shortVersion

    @Language("YAML")
    val jobText = """
      qodana:
        runs-on: ubuntu-latest
        permissions:
          contents: write
          pull-requests: write
          checks: write
        steps:
          - uses: actions/checkout@v3
            with:
              ref: ${refsText}
              fetch-depth: 0
          - name: 'Qodana Scan'
            uses: JetBrains/qodana-action@v$qodanaGitHubActionVersion
            env:
              QODANA_TOKEN: $cloudTokenText
            ${if (baselineText != null) "with:" else ""}
            ${baselineText?.let { "  $it" } ?: ""}
    """.replaceIndent("  ").trimEnd()
    return jobText
  }

  private suspend fun defaultConfigurationText(): String {
    val branchesToAdd = projectVcsDataProvider.ciRelevantBranches()

    @Language("YAML")
    val branchesText = """
      name: Qodana
      on:
        workflow_dispatch:
        pull_request:
        push:
          branches:
      
    """.trimIndent() + branchesToAdd.joinToString(separator = "\n", postfix = "\n") { "      - $it" }

    val jobText = defaultQodanaJobText()

    @Suppress("UnnecessaryVariable")
    @Language("YAML")
    val yamlConfiguration = branchesText + """
      
      jobs:

    """.trimIndent() + jobText
    return yamlConfiguration
  }

  override suspend fun isCIPresentInProject(): Boolean {
    val githubDirectoryIsPresent = readAction { project.guessProjectDir()?.findDirectory(".github") != null }
    if (githubDirectoryIsPresent) return true
    return projectVcsDataProvider.originHost()?.startsWith("github.com") ?: false
  }

  fun isConfigPath(path: Path): Boolean {
    return isInConfigDir(path) && isConfigFileExtension(path)
  }

  private fun isInConfigDir(path: Path): Boolean {
    return path.parent == projectNioPath.resolve(GITHUB_WORKFLOWS_DIR)
  }

  private fun isConfigFileExtension(path: Path): Boolean {
    val fileName = path.fileName.toString()
    return GITHUB_WORKFLOWS_EXTENSIONS.any { extension -> fileName.endsWith(".$extension") }
  }

  private suspend fun firstConfigEditorStateWithValidPath(): CIConfigEditorState {
    return configEditorStateFlow
      .filterNotNull()
      .filter {
        val filePath = it.ciConfigFileState.absoluteNioPath ?: return@filter false
        isConfigPath(filePath)
      }.flowOn(QodanaDispatchers.Default).first()
  }

  private fun createBannerContentProvider(): BannerContentProvider {
    val text = QodanaBundle.message("qodana.add.to.ci.cloud.token.is.required.banner.github.text")
    val getTokenAction = BannerContentProvider.Action(QodanaBundle.message("qodana.add.to.ci.cloud.token.is.required.banner.get.token")) {
      openBrowserWithCurrentQodanaCloudFrontend()
    }
    val howToAddTokenToGitLabCi = BannerContentProvider.Action(QodanaBundle.message("qodana.add.to.ci.cloud.token.is.required.banner.how.add.token")) {
      BrowserUtil.browse("https://www.jetbrains.com/help/qodana/github.html#Qodana+Cloud")
    }
    return BannerContentProvider(text, listOf(getTokenAction, howToAddTokenToGitLabCi), onClose = {
      isBannerVisibleStateFlow.value = false
    })
  }
}