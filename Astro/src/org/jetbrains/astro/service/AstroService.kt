// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.service

import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.library.TypeScriptLibraryProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.astro.lang.AstroFileType

fun isServiceEnabledAndAvailable(project: Project, context: VirtualFile): Boolean {
  return isFileAcceptableForService(context) &&
         isServiceEnabledByContextAndSettings(project, context) &&
         AstroLspExecutableDownloader.getExecutableOrRefresh(project) != null
}

fun isFileAcceptableForService(file: VirtualFile): Boolean {
  if (!TypeScriptLanguageServiceUtil.IS_VALID_FILE_FOR_SERVICE.value(file)) return false
  return file.fileType == AstroFileType.INSTANCE || TypeScriptLanguageServiceUtil.ACCEPTABLE_TS_FILE.value(file)
}

fun isServiceEnabledByContextAndSettings(project: Project, context: VirtualFile): Boolean {
  if (!TypeScriptLanguageServiceUtil.isServiceEnabled(project)) return false
  if (context.fileType != AstroFileType.INSTANCE) return false
  if (TypeScriptLibraryProvider.isLibraryOrBundledLibraryFile(project, context)) return false

  return Registry.`is`("astro.enable.lsp", false)
}
