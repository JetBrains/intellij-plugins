// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.util.concurrency.annotations.RequiresReadLock
import kotlinx.coroutines.future.await

@RequiresReadLock
internal suspend fun resolveConfigForFile(file: PsiFile): PrettierLanguageService.ResolveConfigResult? {
  val project = file.project
  val filePath = if (file.isValid) file.virtualFile.path else null
  if (filePath == null) return null

  val nodePackage = PrettierConfiguration.getInstance(project).getPackage(file)
  val service = PrettierLanguageService.getInstance(project, file.virtualFile, nodePackage)
  val future = service.resolveConfig(filePath, nodePackage)

  return future.await()
}

internal fun ensureConfigsSaved(virtualFiles: List<VirtualFile>, project: Project) {
  val documentManager = FileDocumentManager.getInstance()
  PrettierUtil.lookupPossibleConfigFiles(virtualFiles, project).forEach { config ->
    val document = documentManager.getCachedDocument(config)
    if (document != null && documentManager.isDocumentUnsaved(document)) {
      documentManager.saveDocument(document)
    }
  }
}