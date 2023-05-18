package com.intellij.deno.service

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.intellij.deno.DenoSettings
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.lang.javascript.TypeScriptFileType
import com.intellij.lsp.api.LspServerDescriptor
import com.intellij.lsp.api.LspServerSupportProvider
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

class DenoLspSupportProvider : LspServerSupportProvider {
  override fun fileOpened(project: Project, file: VirtualFile, serverStarter: LspServerSupportProvider.LspServerStarter) {
    getDenoDescriptor(project)?.let { serverStarter.ensureServerStarted(it) }
  }
}

fun getDenoDescriptor(project: Project): DenoLspServerDescriptor? {
  if (DenoSettings.getService(project).isUseDeno()) {
    // TODO don't use guessProjectDir()
    val root = project.guessProjectDir()
    if (root != null) {
      return DenoLspServerDescriptor(project, root)
    }
  }
  return null
}

class DenoLspServerDescriptor(project: Project, vararg roots: VirtualFile) : LspServerDescriptor(project, "Deno", *roots) {

  override fun isSupportedFile(file: VirtualFile) = file.fileType == TypeScriptFileType.INSTANCE

  override fun createCommandLine(): GeneralCommandLine {
    return DenoSettings.getService(project).getDenoPath().ifEmpty { null }?.let {
      GeneralCommandLine(it, "lsp")
    }.also { DenoTypings.getInstance(project).reloadAsync() } ?: throw RuntimeException("deno is not installed")
  }

  override fun createInitializationOptions(): Any {
    val pathMacroManager = PathMacroManager.getInstance(project)
    val denoInit = pathMacroManager.expandPath(DenoSettings.getService(project).getDenoInit())
    val result = JsonParser.parseString(denoInit)
    expandRelativePath(result, "importMap")
    expandRelativePath(result, "config")

    return result
  }

  private fun expandRelativePath(jsonElement: JsonElement, name: String) {
    val basePath = project.basePath
    if (!jsonElement.isJsonObject) return
    val jsonObject = jsonElement.asJsonObject
    val importMap = jsonObject.get(name)
    if (importMap == null || !importMap.isJsonPrimitive) return

    val primitive = importMap.asJsonPrimitive
    if (!primitive.isString) return
    val text = primitive.asString ?: return
    if (File(text).isAbsolute) return
    jsonObject.remove(name)
    jsonObject.add(name, JsonPrimitive(FileUtil.toSystemDependentName("$basePath/$text")))
  }

  override val lspCompletionSupport = null
  override val lspDiagnosticsSupport = null
  override val useGenericNavigation = false
}