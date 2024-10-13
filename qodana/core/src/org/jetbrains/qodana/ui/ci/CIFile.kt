package org.jetbrains.qodana.ui.ci

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import kotlinx.coroutines.flow.*
import org.jetbrains.qodana.coroutines.appearedFilePath
import org.jetbrains.qodana.coroutines.disappearedFilePath
import org.jetbrains.qodana.coroutines.vfsChangesFilterFlow
import org.jetbrains.qodana.ui.ci.providers.CIFileChecker
import org.jetbrains.qodana.ui.ci.providers.toNioPathSafe
import java.nio.file.Path
import kotlin.io.path.pathString

sealed interface CIFile {
  val path: String

  val refreshRequestFlow: Flow<RefreshRequest>

  object RefreshRequest

  class Existing(override val path: String, ciFileChecker: CIFileChecker, virtualFile: VirtualFile?) : CIFile {
    override val refreshRequestFlow: Flow<RefreshRequest> = createFlow(virtualFile, ciFileChecker)

    private fun createFlow(virtualFile: VirtualFile?, ciFileChecker: CIFileChecker): Flow<RefreshRequest> {
      virtualFile ?: return flowOf(RefreshRequest)
      return merge(
        vfsChangesFilterFlow { it is VFileContentChangeEvent && virtualFile.path.startsWith(it.file.path) }
          .filter { ciFileChecker.isQodanaPresent(virtualFile) }
          .map { RefreshRequest },
        vfsChangesFilterFlow { event -> event.disappearedFilePath?.pathString?.let { path.startsWith(it) } ?: false }.map { RefreshRequest },
      )
    }
  }

  class ExistingWithQodana(override val path: String, val ciFileChecker: CIFileChecker, virtualFile: VirtualFile?) : CIFile {
    override val refreshRequestFlow: Flow<RefreshRequest> = createFlow(virtualFile, ciFileChecker)

    private fun createFlow(virtualFile: VirtualFile?, ciFileChecker: CIFileChecker): Flow<RefreshRequest> {
      virtualFile ?: return flowOf(RefreshRequest)
      return merge(
        vfsChangesFilterFlow { it is VFileContentChangeEvent && it.file.path == virtualFile.path }
          .filter { !ciFileChecker.isQodanaPresent(virtualFile) }
          .map { RefreshRequest },
        vfsChangesFilterFlow { event -> event.disappearedFilePath?.pathString?.let { path.startsWith(it) } ?: false }.map { RefreshRequest }
      )
    }
  }

  class NotExisting(override val path: String) : CIFile {
    override val refreshRequestFlow: Flow<RefreshRequest> =
      vfsChangesFilterFlow {
        val toCheck = createAbsoluteNioPath() ?: return@vfsChangesFilterFlow false
        it.appearedFilePath?.startsWith(toCheck) == true
      }.map { RefreshRequest }

    private fun createAbsoluteNioPath(): Path? {
      val path = path.toNioPathSafe() ?: return null
      if (!path.isAbsolute) return null
      return path
    }
  }

  object Empty : CIFile {
    override val path: String = ""
    override val refreshRequestFlow: Flow<RefreshRequest> = emptyFlow()

  }
}