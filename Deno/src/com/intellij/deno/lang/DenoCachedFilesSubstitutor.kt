package com.intellij.deno.lang

import com.intellij.deno.DenoSettings
import com.intellij.deno.DenoUtil
import com.intellij.lang.Language
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.LanguageSubstitutor

class DenoCachedFilesSubstitutor : LanguageSubstitutor() {
  override fun getLanguage(file: VirtualFile, project: Project): Language? {
    if (!DenoSettings.getService(project).isUseDeno()) return null

    val extension = FileUtil.getExtension(file.nameSequence)
    if (extension.isNotEmpty()) return null

    val cache = DenoSettings.getService(project).getDenoCache()
    return if (cache.isNotEmpty() && file.path.startsWith(cache)) JavaScriptSupportLoader.TYPESCRIPT else null
  }
}