package com.intellij.deno.service

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.intellij.deno.DenoSettings
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.lsp.LspServerDescriptor
import com.intellij.lsp.LspServerSupportProvider
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

class DenoLspSupportProvider : LspServerSupportProvider {
  override fun getServerDescriptor(project: Project, virtualFile: VirtualFile): LspServerDescriptor? =
    if (DenoSettings.getService(project).isUseDeno()) {
      getDenoDescriptor(project)
    }
    else {
      null
    }
}

fun getDenoDescriptor(project: Project): DenoLspServerDescriptor {
  return project.getService(DenoLspServerDescriptor::class.java)
}

@Service
class DenoLspServerDescriptor(private val myProject: Project) : LspServerDescriptor(), Disposable {

  override fun getProject(): Project = myProject

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

  override fun getRoot(): VirtualFile = project.guessProjectDir()!!

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

  override fun useGenericCompletion() = false

  override fun useGenericHighlighting() = false

  override fun useGenericNavigation() = false
  override fun dispose() {}
}