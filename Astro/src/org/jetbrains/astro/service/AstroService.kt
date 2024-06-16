// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.service

import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.library.TypeScriptLibraryProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.astro.lang.AstroFileType
import org.jetbrains.astro.service.settings.AstroServiceMode
import org.jetbrains.astro.service.settings.getAstroServiceSettings

fun isServiceEnabledAndAvailable(project: Project, context: VirtualFile): Boolean {
  return isFileAcceptableForService(context) &&
         isServiceEnabledByContextAndSettings(project, context) &&
         AstroLspExecutableDownloader.getExecutableOrRefresh(project) != null
}

fun isFileAcceptableForService(file: VirtualFile): Boolean {
  if (!TypeScriptLanguageServiceUtil.IS_VALID_FILE_FOR_SERVICE.value(file)) return false
  return file.fileType == AstroFileType || TypeScriptLanguageServiceUtil.ACCEPTABLE_TS_FILE.value(file)
}

private fun isServiceEnabledByContextAndSettings(project: Project, context: VirtualFile): Boolean {
  return TypeScriptLanguageServiceUtil.isServiceEnabled(project) &&
         !TypeScriptLibraryProvider.isLibraryOrBundledLibraryFile(project, context) &&
         isEnabledBySettings(project) &&
         context.fileType == AstroFileType
}

private fun isEnabledBySettings(project: Project): Boolean {
  return getAstroServiceSettings(project).serviceMode == AstroServiceMode.ENABLED
}
