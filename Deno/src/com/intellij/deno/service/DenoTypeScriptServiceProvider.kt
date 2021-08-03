package com.intellij.deno.service

import com.intellij.deno.DenoSettings
import com.intellij.lang.javascript.service.JSLanguageService
import com.intellij.lang.javascript.service.JSLanguageServiceProvider
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class DenoTypeScriptServiceProvider(val project: Project) : JSLanguageServiceProvider {

  override fun isHighlightingCandidate(file: VirtualFile) = TypeScriptCompilerSettings.acceptFileType(file.fileType)

  override fun getService(file: VirtualFile) = allServices.firstOrNull()

  override fun getAllServices() =
    if (DenoSettings.getService(project).isUseDeno()) listOf(DenoTypeScriptService.getInstance(project)) else emptyList()
}