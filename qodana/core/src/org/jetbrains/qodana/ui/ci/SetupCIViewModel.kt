package org.jetbrains.qodana.ui.ci

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.daemon.impl.IntentionsUI
import com.intellij.openapi.application.readAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.UiAnyModality
import org.jetbrains.qodana.getFileTypeByFilename
import org.jetbrains.qodana.report.BannerContentProvider
import org.jetbrains.qodana.ui.ci.providers.CIConfigEditorState
import org.jetbrains.qodana.ui.ci.providers.CIConfigFileState
import org.jetbrains.qodana.ui.ci.providers.alreadyContainsQodana
import org.jetbrains.qodana.ui.ci.providers.getPhysicalConfigState
import org.jetbrains.qodana.ui.createEditor
import org.jetbrains.qodana.ui.createInMemoryDocument
import java.nio.file.Path

typealias SetupCIFinishProvider = suspend () -> Unit

interface SetupCIViewModel {
  suspend fun isCIPresentInProject(): Boolean

  val finishProviderFlow: Flow<SetupCIFinishProvider?>

  fun unselected()
}

@OptIn(ExperimentalCoroutinesApi::class)
class BaseSetupCIViewModel(
  private val projectNioPath: Path,
  private val project: Project,
  private val configFileName: String,
  private val scope: CoroutineScope,
  private val actions: Actions,
  private val disableHighlighting: Boolean = false
) {
  val configEditorStateFlow: StateFlow<CIConfigEditorState?> = createConfigEditorStateFlow()

  val isBannerVisibleStateFlow = MutableStateFlow(true)
  val bannerContentProviderFlow: Flow<BannerContentProvider?> =
    isBannerVisibleStateFlow.map { if (it) actions.createBannerContentProvider() else null }

  fun createFinishProviderFlow(): Flow<SetupCIFinishProvider?> {
    return configEditorStateFlow.filterNotNull()
      .map {
        val finishProviderSpawningNotification = suspend {
          it.ciConfigFileState.writeFile()
          actions.spawnAddedConfigurationNotification()
        }
        finishProviderSpawningNotification
      }
  }

  private val configFiletype: FileType = getFileTypeByFilename(configFileName)

  fun unselected() {
    val editor = configEditorStateFlow.value?.editor ?: return
    scope.launch(QodanaDispatchers.Ui) {
      IntentionsUI.getInstance(project).hideForEditor(editor)
    }
  }

  private fun createConfigEditorStateFlow(): StateFlow<CIConfigEditorState?> {
    val configEditorStateFlow = MutableStateFlow<CIConfigEditorState?>(null)
    scope.launch(QodanaDispatchers.Default) {
      val editorsToRelease = mutableListOf<Editor>()
      try {
        configEditorStateFlow
          .flatMapLatest { it?.ciConfigFileState?.refreshConfigFileRequestFlow ?: flowOf(CIConfigFileState.RefreshRequest.FindFile) }
          .collectLatest {
            val newConfigFileState = refreshConfigFileState()
            val newEditor = createEditor(project, newConfigFileState.document, configFiletype)
            if (disableHighlighting) {
              val psiFile = readAction {
                PsiDocumentManager.getInstance(project).getPsiFile(newEditor.document)
              }
              psiFile?.let { DaemonCodeAnalyzer.getInstance(project).setHighlightingEnabled(it, false) }
            }
            editorsToRelease.add(newEditor)
            configEditorStateFlow.value = CIConfigEditorState(newEditor, newConfigFileState)
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
    return configEditorStateFlow.asStateFlow()
  }

  private suspend fun refreshConfigFileState(): CIConfigFileState {
    val physicalConfigFile = readAction { project.guessProjectDir()?.findFileByRelativePath(configFileName) }
    return when {
      physicalConfigFile != null && physicalConfigFile.alreadyContainsQodana() -> {
        getPhysicalConfigState(project, physicalConfigFile) ?: getInMemoryConfig()
      }
      physicalConfigFile != null -> {
        actions.getInMemoryPatchOfPhysicalConfig?.invoke(physicalConfigFile) ?: getInMemoryPatchOfPhysicalConfigBase(physicalConfigFile) ?: getInMemoryConfig()
      }
      else -> {
        getInMemoryConfig()
      }
    }
  }

  private suspend fun getInMemoryPatchOfPhysicalConfigBase(configFile: VirtualFile): CIConfigFileState.InMemoryPatchOfPhysicalFile? {
    val physicalDocument = readAction { FileDocumentManager.getInstance().getDocument(configFile) } ?: return null
    val physicalText = physicalDocument.immutableCharSequence
    val contentOfInMemoryPatchDocument = listOf(actions.defaultConfigurationText(), physicalText).joinToString("\n\n")
    val inMemoryPatchDocument = createInMemoryDocument(project, contentOfInMemoryPatchDocument, configFile.name)

    return CIConfigFileState.InMemoryPatchOfPhysicalFile(project, inMemoryPatchDocument, physicalDocument, configFile.toNioPath())
  }

  private suspend fun getInMemoryConfig(): CIConfigFileState.InMemory {
    val inMemoryDocument = createInMemoryDocument(project, actions.defaultConfigurationText(), configFileName)
    val absoluteStringPath = projectNioPath.resolve(configFileName).toString()

    return CIConfigFileState.InMemory(project, inMemoryDocument, absoluteStringPath)
  }

  class Actions(
    val createBannerContentProvider: () -> BannerContentProvider,
    val spawnAddedConfigurationNotification: () -> Unit,
    val defaultConfigurationText: suspend () -> String,
    val getInMemoryPatchOfPhysicalConfig: (suspend (VirtualFile) -> CIConfigFileState.InMemoryPatchOfPhysicalFile?)?,
  )
}