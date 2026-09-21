package org.jetbrains.qodana.inspectionKts

import com.intellij.ide.trustedProjects.TrustedProjects
import com.intellij.ide.trustedProjects.TrustedProjectsListener
import com.intellij.openapi.application.readAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFileManager
import kotlinx.coroutines.suspendCancellableCoroutine
import org.jetbrains.annotations.ApiStatus
import java.nio.file.Path

@ApiStatus.Internal
suspend fun getDocumentByNioPath(file: Path): Document? {
  val virtualFile = VirtualFileManager.getInstance().findFileByNioPath(file)
  val document = if (virtualFile != null) {
    readAction {
      FileDocumentManager.getInstance().getDocument(virtualFile)
    }
  } else {
    null
  }
  return document
}

@ApiStatus.Internal
suspend fun waitWhenProjectTrusted(project: Project) {
  if (TrustedProjects.isProjectTrusted(project)) return

  suspendCancellableCoroutine { cont ->
    val disposable = Disposer.newDisposable()
    cont.invokeOnCancellation {
      Disposer.dispose(disposable)
    }
    TrustedProjectsListener.onceWhenProjectTrusted(disposable) {
      cont.resumeWith(Result.success(Unit))
    }
  }
}
