// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.actions

import com.intellij.openapi.application.EDT
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ide.progress.withBackgroundProgress
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.intellij.terraform.config.util.TFExecutor
import org.intellij.terraform.config.util.executeSuspendable
import org.jetbrains.annotations.Nls

class TFFmtFileAction : TFExternalToolsAction() {

  override suspend fun invoke(project: Project, title: @Nls String, virtualFile: VirtualFile) {
    withBackgroundProgress(project, title) {
      withContext(Dispatchers.EDT) {
        val document = FileDocumentManager.getInstance().getDocument(virtualFile)
        if (document != null) {
          FileDocumentManager.getInstance().saveDocument(document)
        }
        else {
          FileDocumentManager.getInstance().saveAllDocuments()
        }
      }

      val filePath = virtualFile.canonicalPath!!
      TFExecutor.`in`(project)
        .withPresentableName(title)
        .withParameters("fmt", filePath)
        .showOutputOnError()
        .withWorkDirectory(virtualFile.parent.canonicalPath)
        .executeSuspendable()
      VfsUtil.markDirtyAndRefresh(true, true, true, virtualFile)
    }

  }

  fun scheduleFormatFile(project: Project, title: @Nls String, virtualFile: VirtualFile): Deferred<Unit> {
    return getActionCoroutineScope(project).async {
      invoke(project, title, virtualFile)
    }
  }
}
