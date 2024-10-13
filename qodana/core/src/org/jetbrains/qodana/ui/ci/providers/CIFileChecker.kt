package org.jetbrains.qodana.ui.ci.providers

import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.ui.ci.CIFile
import kotlin.io.path.Path
import kotlin.io.path.pathString

interface CIFileChecker {
  val ciFileFlow: Flow<CIFile?>

  suspend fun isQodanaPresent(virtualFile: VirtualFile): Boolean

  val ciPart: String
}

internal fun createRefreshSingleCIFile(
  project: Project,
  configFileName: String,
  ciFileChecker: CIFileChecker
): suspend () -> CIFile {
  return function@{
    val projectPath = project.guessProjectDir() ?: return@function CIFile.Empty
    val physicalConfigFile = readAction { projectPath.findFileByRelativePath(configFileName) }
    val configAbsolutePath = Path(projectPath.path).resolve(configFileName)

    when {
      physicalConfigFile != null -> {
        val containsQodana = ciFileChecker.isQodanaPresent(physicalConfigFile)
        if (containsQodana) {
          CIFile.ExistingWithQodana(configAbsolutePath.pathString, ciFileChecker, physicalConfigFile)
        }
        else {
          CIFile.Existing(configAbsolutePath.pathString, ciFileChecker, physicalConfigFile)
        }
      }
      else -> CIFile.NotExisting(configAbsolutePath.pathString)
    }
  }
}

@OptIn(ExperimentalCoroutinesApi::class)
internal fun createCIFileFlow(scope: CoroutineScope, refreshCIFile: suspend () -> CIFile): Flow<CIFile?> {
  val flow = MutableStateFlow<CIFile?>(null)
  scope.launch(QodanaDispatchers.Default) {
    flow
      .flatMapLatest { it?.refreshRequestFlow ?: flowOf(CIFile.RefreshRequest) }
      .collectLatest { flow.value = refreshCIFile() }
  }
  return flow
}
