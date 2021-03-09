package com.intellij.deno.service

import com.intellij.deno.DenoSettings
import com.intellij.lang.javascript.service.JSLanguageService
import com.intellij.lang.javascript.service.JSLanguageServiceProvider
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class DenoTypeScriptServiceProvider(val project: Project) : JSLanguageServiceProvider {
  
  override fun isHighlightingCandidate(file: VirtualFile): Boolean {
    return TypeScriptCompilerSettings.acceptFileType(file.fileType)
  }

  override fun getService(file: VirtualFile): JSLanguageService? {
    return if (DenoSettings.getService(project).isUseDeno()) DenoTypeScriptService.getInstance(project) else null
  }
}