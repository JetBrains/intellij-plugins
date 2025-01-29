package org.jetbrains.qodana.ui.run

import com.fasterxml.jackson.core.JacksonException
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.toNioPathOrNull
import com.intellij.openapi.vfs.writeText
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.*
import org.jetbrains.qodana.findQodanaConfigVirtualFile
import org.jetbrains.qodana.getFileTypeByFilename
import org.jetbrains.qodana.refreshVcsFileStatus
import org.jetbrains.qodana.settings.QodanaConfigChangeService
import org.jetbrains.qodana.staticAnalysis.inspections.config.QODANA_CONFIG_FILES
import org.jetbrains.qodana.staticAnalysis.inspections.config.QODANA_YAML_CONFIG_FILENAME
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaYamlReader
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.jetbrains.qodana.ui.createEditor
import org.jetbrains.qodana.ui.createInMemoryDocument
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.pathString

@OptIn(ExperimentalCoroutinesApi::class)
class QodanaYamlViewModelImpl(override val project: Project, private val scope: CoroutineScope) : QodanaYamlViewModel {
  override val yamlStateFlow: StateFlow<QodanaYamlViewModel.YamlState?> = createYamlStateFlow()

  private val yamlFiletype: FileType = getFileTypeByFilename(QODANA_YAML_CONFIG_FILENAME)

  private val yamlErrorHappenedFlow = MutableSharedFlow<QodanaYamlViewModel.ParseResult.Error>()
  override val yamlValidationErrorFlow: Flow<QodanaYamlViewModel.ParseResult.Error?> = merge(
    yamlErrorHappenedFlow,
    yamlStateFlow.filterNotNull().flatMapLatest { merge(flowOf(Unit), documentChangesFlow(it.document)) }.map { null }
  )

  override fun parseQodanaYaml(): Deferred<QodanaYamlViewModel.ParseResult?> {
    return scope.async(QodanaDispatchers.Default) {
      val yamlState = yamlStateFlow.value ?: return@async null
      val yamlContent = yamlState.document.immutableCharSequence
      val projectPath = project.guessProjectDir()?.toNioPath()?.pathString ?: return@async null
      val parseResult = parseQodanaYamlConfig(yamlContent.toString(), projectPath, yamlState.physicalFile)
      if (parseResult is QodanaYamlViewModel.ParseResult.Error) {
        yamlErrorHappenedFlow.emit(parseResult)
      }
      parseResult
    }
  }

  override fun writeQodanaYamlIfNeeded(): Deferred<Path?> {
    return project.qodanaProjectScope.async(QodanaDispatchers.Default) {
      val yamlState = yamlStateFlow.value ?: return@async null
      val projectVirtualFile = project.guessProjectDir() ?: return@async null
      if (yamlState.isPhysical) {
        return@async project.findQodanaConfigVirtualFile()?.toNioPath()
      }

      val newVirtualFile = writeAction {
        val newVirtualFile = projectVirtualFile.createChildData(projectVirtualFile.fileSystem, QODANA_YAML_CONFIG_FILENAME)
        newVirtualFile.writeText(yamlState.document.text)
        newVirtualFile
      }
      refreshVcsFileStatus(project, newVirtualFile)
      return@async newVirtualFile.toNioPath()
    }
  }

  private fun createYamlStateFlow(): StateFlow<QodanaYamlViewModel.YamlState?> {
    val yamlStateFlow = MutableStateFlow<QodanaYamlViewModel.YamlState?>(null)
    scope.launch(QodanaDispatchers.Default) {
      val editorsToRelease = mutableListOf<Editor>()
      try {
        merge(flowOf(Unit), physicalQodanaYamlFileChangesFlow()).collectLatest {
          val yamlState = refreshQodanaYamlState()
          editorsToRelease.add(yamlState.editor)
          yamlStateFlow.value = yamlState
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
    return yamlStateFlow.asStateFlow()
  }

  private fun physicalQodanaYamlFileChangesFlow(): Flow<Unit> {
    return vfsChangesFilterFlow {
      val appearedFile = it.appearedFilePath
      val disappearedFile = it.disappearedFilePath

      (appearedFile?.isQodanaYaml(project) == true || disappearedFile?.isQodanaYaml(project) == true)
    }
  }

  private suspend fun refreshQodanaYamlState(): QodanaYamlViewModel.YamlState {
    val qodanaYamlFromRoot = getQodanaYamlFromProjectRoot()
    if (qodanaYamlFromRoot != null) {
      val editor = createEditor(project, qodanaYamlFromRoot.first, yamlFiletype)
      return QodanaYamlViewModel.YamlState(document = qodanaYamlFromRoot.first, editor, physicalFile = qodanaYamlFromRoot.second)
    }

    val inMemoryYamlContent = QodanaConfigChangeService.getInstance(project).createDefaultConfigContent()
    val inMemoryYaml = createInMemoryDocument(project, inMemoryYamlContent, QODANA_YAML_CONFIG_FILENAME)
    val editor = createEditor(project, inMemoryYaml, yamlFiletype)
    return QodanaYamlViewModel.YamlState(inMemoryYaml, editor, physicalFile = null)
  }

  private suspend fun getQodanaYamlFromProjectRoot(): Pair<Document, Path>? {
    return withContext(QodanaDispatchers.Default) {
      val projectDir = project.guessProjectDir()

      QODANA_CONFIG_FILES
        .mapNotNull { projectDir?.findChild(it) }
        .firstNotNullOfOrNull { file ->
          val nioPath = file.toNioPathOrNull() ?: return@firstNotNullOfOrNull null
          val document = readAction {
            FileDocumentManager.getInstance().getDocument(file)
          } ?: return@firstNotNullOfOrNull null
          document to nioPath
        }
    }
  }

  private fun Path.isQodanaYaml(project: Project): Boolean {
    val projectDir = project.guessProjectDir()?.toNioPath() ?: return false
    return QODANA_CONFIG_FILES.map(projectDir::resolve).contains(this)
  }

  private fun parseQodanaYamlConfig(
    text: String,
    projectPath: String,
    yamlPath: Path?,
  ): QodanaYamlViewModel.ParseResult {
    val projectPath = Path(projectPath)
    val yamlPath = yamlPath ?: projectPath.resolve(QODANA_YAML_CONFIG_FILENAME)

    return QodanaYamlReader.parse(text)
      .map { it.withAbsoluteProfilePath(projectPath, yamlPath) }
      .map { QodanaYamlViewModel.ParseResult.Valid(it, text) }
      .getOrElse { e ->
        when (e) {
          is JacksonException -> {
            val location = e.location ?: return@getOrElse QodanaYamlViewModel.ParseResult.Error(
              QodanaBundle.message("qodana.failed.parse.qodana.yaml.no.details"))
            val error = e.originalMessage
            val line = location.offsetDescription()
            val snippet = location.sourceDescription()
            val message = QodanaBundle.message("qodana.failed.parse.qodana.yaml.with.details", error, line, snippet)
            QodanaYamlViewModel.ParseResult.Error(message)
          }
          is QodanaException -> QodanaYamlViewModel.ParseResult.Error(
            QodanaBundle.message("qodana.failed.parse.qodana.yaml.no.details"))
          else -> throw e
        }
      }
  }

}
