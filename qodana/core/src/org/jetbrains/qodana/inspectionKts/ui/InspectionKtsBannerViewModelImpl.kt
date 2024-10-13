package org.jetbrains.qodana.inspectionKts.ui

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFileManager
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.inspectionKts.InspectionKtsErrorLogManager
import org.jetbrains.qodana.inspectionKts.InspectionKtsFileStatus
import org.jetbrains.qodana.inspectionKts.KtsInspectionsManager
import org.jetbrains.qodana.inspectionKts.examples.InspectionKtsExample
import org.jetbrains.qodana.ui.createEditor
import java.net.URL
import java.nio.file.Path


@OptIn(ExperimentalCoroutinesApi::class)
class InspectionKtsBannerViewModelImpl(
  val file: Path,
  private val project: Project,
  private val scope: CoroutineScope,
  private val inspectionKtsManager: KtsInspectionsManager
) : InspectionKtsBannerViewModel {

  override val compilationStatus: StateFlow<InspectionKtsBannerViewModel.CompilationStatus?> = compilationStatusFlow()
    .stateIn(scope + QodanaDispatchers.Default, SharingStarted.Eagerly, null)

  override val psiViewerOpener: InspectionKtsBannerViewModel.PsiViewerOpener? = psiViewerOpener()

  override val examples: List<InspectionKtsBannerViewModel.Example> = examplesList()

  private fun compilationStatusFlow(): Flow<InspectionKtsBannerViewModel.CompilationStatus?> {
    return inspectionKtsManager.ktsInspectionsFlow
      .filterNotNull()
      .map { statuses ->
        statuses.find { it.file == file }
      }
      .distinctUntilChanged()
      .transformLatest { status ->
        coroutineScope {
          val compilationStatus = when(status) {
            is InspectionKtsFileStatus.Cancelled -> InspectionKtsBannerViewModel.CompilationStatus.Cancelled(::doRecompile)
            is InspectionKtsFileStatus.Compiling -> InspectionKtsBannerViewModel.CompilationStatus.Compiling(::doRecompile)
            is InspectionKtsFileStatus.Error -> failedCompilationStatus(status)
            is InspectionKtsFileStatus.Compiled -> successCompilationStatus(this@coroutineScope, status)
            null -> null
          }
          emit(compilationStatus)
        }
      }
  }

  private fun failedCompilationStatus(error: InspectionKtsFileStatus.Error): InspectionKtsBannerViewModel.CompilationStatus.Failed {
    return InspectionKtsBannerViewModel.CompilationStatus.Failed(
      error.isOutdated,
      openExceptionInLogAction = {
        openExceptionLogInEditorAsync(error.errorInLogProvider, error.exception)
      },
      ::doRecompile
    )
  }

  private fun successCompilationStatus(
    scope: CoroutineScope,
    compiled: InspectionKtsFileStatus.Compiled
  ): InspectionKtsBannerViewModel.CompilationStatus.Compiled {
    val clearExceptionFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    val initialException = compiled.exceptionDuringAnalysis.value
    val executionErrorDuringAnalysis = merge(compiled.exceptionDuringAnalysis, clearExceptionFlow.map { null })
      .map { exception ->
        exception?.let { exceptionToExecutionError(compiled, it, clearExceptionFlow) }
      }
      .stateIn(scope, SharingStarted.Lazily, initialException?.let { exceptionToExecutionError(compiled, it, clearExceptionFlow) })

    return InspectionKtsBannerViewModel.CompilationStatus.Compiled(executionErrorDuringAnalysis, compiled.isOutdated, ::doRecompile)
  }

  private fun openExceptionLogInEditorAsync(errorInLogProvider: InspectionKtsErrorLogManager.ErrorInLogProvider, exception: Exception) {
    scope.launch(QodanaDispatchers.Default) {
      val exceptionLocationInLogFile = errorInLogProvider.loggedExceptionLocation(exception) ?: return@launch
      val logFileVirtualFile = LocalFileSystem.getInstance().findFileByNioFile(exceptionLocationInLogFile.file) ?: return@launch

      withContext(QodanaDispatchers.Ui) {
        OpenFileDescriptor(project, logFileVirtualFile, exceptionLocationInLogFile.line, 0).navigate(true)
      }
    }
  }

  private fun exceptionToExecutionError(
    compiled: InspectionKtsFileStatus.Compiled,
    exception: Exception,
    clearExceptionFlow: MutableSharedFlow<Unit>
  ): InspectionKtsBannerViewModel.ExecutionError {
    return InspectionKtsBannerViewModel.ExecutionError(
      openExceptionInLogAction = {
        openExceptionLogInEditorAsync(compiled.errorInLogProvider, exception)
      },
      ignoreExceptionAction = {
        clearExceptionFlow.tryEmit(Unit)
      }
    )
  }

  private fun doRecompile() {
    inspectionKtsManager.recompileFile(file)
  }

  private fun psiViewerOpener(): InspectionKtsBannerViewModel.PsiViewerOpener? {
    val psiViewerSupport = PsiViewerSupport.EP_NAME.extensionList.firstOrNull() ?: return null
    return InspectionKtsBannerViewModel.PsiViewerOpener {
      chooseFileAndOpenPsiViewer(psiViewerSupport)
    }
  }

  private fun chooseFileAndOpenPsiViewer(psiViewerSupport: PsiViewerSupport) {
    scope.launch(QodanaDispatchers.Ui) {
      val projectDir = project.guessProjectDir()
      val chooserDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor().let {
        if (projectDir == null) it else it.withRoots(projectDir)
      }
      val file = FileChooserFactory.getInstance().createFileChooser(
        chooserDescriptor,
        project,
        null
      ).choose(project).firstOrNull() ?: return@launch
      val document = FileDocumentManager.getInstance().getDocument(file) ?: return@launch
      val editor = createEditor(project, document, file.fileType)
      psiViewerSupport.openPsiViewerDialog(project, editor)
    }
  }

  private fun examplesList(): List<InspectionKtsBannerViewModel.Example> {
    return InspectionKtsExample.Provider.examples().map { example ->
      InspectionKtsBannerViewModel.Example(
        icon = example.icon,
        text = example.text,
        openExampleAction = { openExampleInEditor(example.resourceUrl) }
      )
    }
  }

  private fun openExampleInEditor(url: URL) {
    scope.launch(QodanaDispatchers.Default) {
      val vfsUrl = VfsUtilCore.convertFromUrl(url)
      val file = VirtualFileManager.getInstance().refreshAndFindFileByUrl(vfsUrl) ?: return@launch
      withContext(QodanaDispatchers.Ui) {
        OpenFileDescriptor(project, file, 0).navigate(true)
      }
    }
  }
}