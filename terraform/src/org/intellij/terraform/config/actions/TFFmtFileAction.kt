// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.actions

import com.intellij.openapi.application.EDT
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ide.progress.withBackgroundProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.intellij.terraform.config.util.TFExecutor
import org.intellij.terraform.config.util.executeSuspendable
import org.jetbrains.annotations.Nls

class TFFmtFileAction : TFExternalToolsAction() {

  override suspend fun invoke(project: Project, title: @Nls String, vararg virtualFiles: VirtualFile) {
    withBackgroundProgress(project, title) {
      withContext(Dispatchers.EDT) {
        FileDocumentManager.getInstance().saveAllDocuments()
      }

      val filePaths = virtualFiles.map { it.canonicalPath!! }.toTypedArray()
      TFExecutor.`in`(project)
        .withPresentableName(title)
        .withParameters("fmt", *filePaths)
        .showOutputOnError()
        .executeSuspendable()
      VfsUtil.markDirtyAndRefresh(true, true, true, *virtualFiles)
    }
  }
}
