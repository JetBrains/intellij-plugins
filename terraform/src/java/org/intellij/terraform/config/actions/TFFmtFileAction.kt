// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.actions

import com.intellij.execution.ExecutionException
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.EmptyConsumer
import org.intellij.terraform.config.util.TFExecutor
import org.jetbrains.annotations.Nls

class TFFmtFileAction : TFExternalToolsAction() {
  fun createExecutor(project: Project, module: Module?, title: @Nls String, virtualFile: VirtualFile): TFExecutor {
    val filePath = virtualFile.canonicalPath!!
    return TFExecutor.`in`(project, module)
      .withPresentableName(title)
      .withParameters("fmt", filePath)
      .showOutputOnError()
      .withWorkDirectory(virtualFile.parent.canonicalPath)
  }


  @Throws(ExecutionException::class)
  override fun invoke(project: Project,
                      module: Module?,
                      title: @Nls String,
                      virtualFile: VirtualFile) {
    val document = FileDocumentManager.getInstance().getDocument(virtualFile)
    if (document != null) {
      FileDocumentManager.getInstance().saveDocument(document)
    }
    else {
      FileDocumentManager.getInstance().saveAllDocuments()
    }

    createExecutor(project, module, title, virtualFile).executeWithProgress(false
    ) { aBoolean: Boolean ->
      EmptyConsumer.getInstance<Boolean>().consume(aBoolean)
      VfsUtil.markDirtyAndRefresh(true, true, true, virtualFile)
    }
  }
}