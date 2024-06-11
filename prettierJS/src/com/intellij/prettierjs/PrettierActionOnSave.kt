// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.codeInsight.actions.onSave.FormatOnSaveOptions
import com.intellij.ide.actionsOnSave.impl.ActionsOnSaveFileDocumentManagerListener.ActionOnSave
import com.intellij.lang.javascript.linter.GlobPatternUtil
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.util.SlowOperations

internal class PrettierActionOnSave : ActionOnSave() {
  override fun isEnabledForProject(project: Project): Boolean = PrettierConfiguration.getInstance(project).isRunOnSave

  override fun processDocuments(project: Project, documents: Array<Document>) {
    val prettierConfiguration = PrettierConfiguration.getInstance(project)
    if (!prettierConfiguration.isRunOnSave) return

    val manager = FileDocumentManager.getInstance()
    val files = documents.mapNotNull { document: Document ->
      val file = manager.getFile(document)
      if (file != null && prettierConfiguration.isRunOnReformat) {
        val onSaveOptions = FormatOnSaveOptions.getInstance(project)
        if (onSaveOptions.isRunOnSaveEnabled && onSaveOptions.isFileTypeSelected(file.fileType)) {
          // already processed as com.intellij.prettierjs.PrettierPostFormatProcessor
          return@mapNotNull null
        }
      }
      file
    }

    SlowOperations.knownIssue("IDEA-322963, EA-845939").use { _ ->
      val matchingFiles = GlobPatternUtil.filterFilesMatchingGlobPattern(project, prettierConfiguration.filesPattern, files)
      if (!matchingFiles.isEmpty()) {
        ReformatWithPrettierAction.processVirtualFiles(project, matchingFiles, ReformatWithPrettierAction.NOOP_ERROR_HANDLER)
      }
    }
  }
}
