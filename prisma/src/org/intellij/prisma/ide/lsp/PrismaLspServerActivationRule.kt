// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.lsp

import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.lsp.LspServerActivationRule
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.intellij.prisma.lang.PrismaFileType

object PrismaLspServerActivationRule : LspServerActivationRule(PrismaLspServerLoader) {
  override fun isFileAcceptableForLspServer(file: VirtualFile): Boolean {
    if (!TypeScriptLanguageServiceUtil.IS_VALID_FILE_FOR_SERVICE.value(file)) return false
    return file.fileType == PrismaFileType
  }

  override fun isProjectContext(project: Project, context: VirtualFile): Boolean =
    context.fileType == PrismaFileType

  override fun isEnabledInSettings(project: Project): Boolean =
    PrismaServiceSettings.getInstance(project).serviceMode == PrismaServiceMode.ENABLED
}