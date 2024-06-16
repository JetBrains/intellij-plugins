// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.service

import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.library.TypeScriptLibraryProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.astro.lang.AstroFileType
import org.jetbrains.astro.service.settings.AstroServiceMode
import org.jetbrains.astro.service.settings.getAstroServiceSettings


/**
 * Checks if the file is local and of the correct file type.
 */
fun isFileAcceptableForLspServer(file: VirtualFile): Boolean {
  if (!TypeScriptLanguageServiceUtil.IS_VALID_FILE_FOR_SERVICE.value(file)) return false
  return file.fileType == AstroFileType || TypeScriptLanguageServiceUtil.ACCEPTABLE_TS_FILE.value(file)
}

/**
 * If enabled but not available, will launch a background task that will eventually restart the services
 */
fun isLspServerEnabledAndAvailable(project: Project, context: VirtualFile): Boolean {
  return isFileAcceptableForLspServer(context) &&
         isServiceEnabledByContextAndSettings(project, context) &&
         AstroLspExecutableDownloader.getExecutableOrRefresh(project) != null
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
