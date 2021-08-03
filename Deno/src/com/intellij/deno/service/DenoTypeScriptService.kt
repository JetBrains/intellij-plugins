package com.intellij.deno.service

import com.intellij.lang.javascript.service.protocol.JSLanguageServiceProtocol
import com.intellij.lang.typescript.compiler.languageService.TypeScriptServerServiceImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.Consumer

class DenoTypeScriptService(project: Project) : TypeScriptServerServiceImpl(project, "Deno Console")  {
  companion object {
    fun getInstance(project: Project): DenoTypeScriptService {
      return project.getService(DenoTypeScriptService::class.java)
    }
  }

  override fun createProtocol(readyConsumer: Consumer<*>, tsServicePath: String): JSLanguageServiceProtocol {
    return DenoTypeScriptServiceProtocol(myProject, mySettings, readyConsumer, createEventConsumer(), "deno-typescript",
                                         tsServicePath)
  }

  override fun skipConfigNotFoundError(file: VirtualFile): Boolean {
    return true
  }
}