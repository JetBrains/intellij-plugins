package org.jetbrains.qodana.ui.ci.providers

import com.intellij.openapi.application.writeAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.findOrCreateFile
import com.intellij.openapi.vfs.writeText
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import org.jetbrains.qodana.coroutines.appearedFilePath
import org.jetbrains.qodana.coroutines.disappearedFilePath
import org.jetbrains.qodana.coroutines.documentChangesFlow
import org.jetbrains.qodana.coroutines.vfsChangesFilterFlow
import org.jetbrains.qodana.refreshVcsFileStatus
import java.nio.file.Path
import kotlin.io.path.relativeTo
import kotlin.time.Duration.Companion.seconds

class CIConfigEditorState(val editor: Editor, val ciConfigFileState: CIConfigFileState)

sealed class CIConfigFileState(val project: Project, val document: Document, val stringPath: String) {
  sealed interface RefreshRequest {
    object FindFile : RefreshRequest

    class SetFile(val newStringPath: String, val ignoreIfPathNotChanged: Boolean = false) : RefreshRequest
  }

  abstract val absoluteNioPath: Path?

  abstract val refreshConfigFileRequestFlow: Flow<RefreshRequest>

  abstract suspend fun writeFile()

  class InMemory(project: Project, document: Document, stringPath: String) : CIConfigFileState(project, document, stringPath) {
    override val absoluteNioPath: Path? = createAbsoluteNioPath()

    override val refreshConfigFileRequestFlow: Flow<RefreshRequest> =
      vfsChangesFilterFlow { it.appearedFilePath == absoluteNioPath }.map { RefreshRequest.SetFile(stringPath) }

    private fun createAbsoluteNioPath(): Path? {
      val path = stringPath.toNioPathSafe() ?: return null
      if (!path.isAbsolute) return null
      return path
    }

    override suspend fun writeFile() {
      val nioPath = absoluteNioPath ?: return
      createAndWriteToFile(project, nioPath, document.text)
    }
  }

  class InMemoryPatchOfPhysicalFile(
    project: Project,
    document: Document,
    physicalDocument: Document,
    override val absoluteNioPath: Path
  ) : CIConfigFileState(project, document, absoluteNioPath.toString()) {
    @OptIn(FlowPreview::class)
    override val refreshConfigFileRequestFlow: Flow<RefreshRequest> = merge(
      documentChangesFlow(physicalDocument).debounce(5.seconds).map { RefreshRequest.SetFile(stringPath) },
      vfsChangesFilterFlow { it.disappearedFilePath == absoluteNioPath }.map { RefreshRequest.FindFile }
    )

    override suspend fun writeFile() {
      createAndWriteToFile(project, absoluteNioPath, document.text)
    }
  }

  class Physical(
    project: Project,
    document: Document,
    override val absoluteNioPath: Path
  ) : CIConfigFileState(project, document, absoluteNioPath.toString()) {
    override val refreshConfigFileRequestFlow: Flow<RefreshRequest> =
      vfsChangesFilterFlow { it.disappearedFilePath == absoluteNioPath }.map { RefreshRequest.FindFile }

    override suspend fun writeFile() {
    }
  }
}

private suspend fun createAndWriteToFile(project: Project, absolutePath: Path, content: String) {
  val virtualFile = writeAction {
    val root = absolutePath.root ?: return@writeAction null
    val rootVirtualFile = LocalFileSystem.getInstance().findFileByNioFile(root) ?: return@writeAction null
    val newVirtualFile = rootVirtualFile.findOrCreateFile(absolutePath.relativeTo(rootVirtualFile.toNioPath()).toString())
    newVirtualFile.writeText(content)
    newVirtualFile
  } ?: return
  refreshVcsFileStatus(project, virtualFile)
}
