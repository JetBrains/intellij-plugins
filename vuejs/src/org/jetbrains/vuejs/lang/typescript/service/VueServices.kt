// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service

import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServerState
import com.intellij.lang.typescript.library.TypeScriptLibraryProvider
import com.intellij.lang.typescript.lsp.getTypeScriptServiceDirectory
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.lang.html.VueFileType.Companion.isDotVueFile
import org.jetbrains.vuejs.lang.typescript.service.volar.VolarExecutableDownloader
import org.jetbrains.vuejs.options.VueServiceSettings
import org.jetbrains.vuejs.options.getVueSettings


private fun isVueServiceContext(project: Project, context: VirtualFile): Boolean = context.fileType is VueFileType || isVueContext(context, project)

private fun isTypeScriptServiceBefore5Context(project: Project): Boolean {
  val path = getTypeScriptServiceDirectory(project)

  val packageJson = TypeScriptServerState.getPackageJsonFromServicePath(path)
  if (packageJson == null) return true
  val version = PackageJsonData.getOrCreate(packageJson).version ?: return true
  return version.major < 5
}

/**
 * Refers to the classic service that predates Volar.
 */
fun isVueTypeScriptServiceEnabled(project: Project, context: VirtualFile): Boolean {
  if (!isVueServiceContext(project, context)) return false

  return when (getVueSettings(project).serviceType) {
    VueServiceSettings.AUTO -> isTypeScriptServiceBefore5Context(project)
    VueServiceSettings.TS_SERVICE -> isTypeScriptServiceBefore5Context(project)
    else -> false
  }
}

/**
 * If enabled but not available, will launch a background task that will eventually restart the services
 */
fun isVolarEnabledAndAvailable(project: Project, context: VirtualFile): Boolean {
  return isVolarFileTypeAcceptable(context) &&
         isVolarEnabledByContextAndSettings(project, context) &&
         VolarExecutableDownloader.getExecutableOrRefresh(project) != null
}

fun isVolarFileTypeAcceptable(file: VirtualFile): Boolean {
  if (!TypeScriptLanguageServiceUtil.IS_VALID_FILE_FOR_SERVICE.value(file)) return false

  return file.isDotVueFile || TypeScriptLanguageServiceUtil.ACCEPTABLE_TS_FILE.value(file)
}

private fun isVolarEnabledByContextAndSettings(project: Project, context: VirtualFile): Boolean {
  if (!TypeScriptLanguageServiceUtil.isServiceEnabled(project)) return false
  if (!isVueServiceContext(project, context)) return false
  if (TypeScriptLibraryProvider.isLibraryOrBundledLibraryFile(project, context)) return false

  return when (getVueSettings(project).serviceType) {
    VueServiceSettings.VOLAR -> true
    VueServiceSettings.AUTO -> !isTypeScriptServiceBefore5Context(project)
    else -> false
  }
}

fun restartTypeScriptServicesAsync(project: Project) {
  ApplicationManager.getApplication().invokeLater(Runnable {
    TypeScriptService.restartServices(project)
  }, project.disposed)
}
