// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.typescript.service

import com.intellij.lang.javascript.service.JSLanguageService
import com.intellij.lang.javascript.service.JSLanguageServiceProvider
import com.intellij.lang.typescript.compiler.TypeScriptLanguageServiceProvider
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.vuejs.lang.html.VueFileType.Companion.isDotVueFile
import org.jetbrains.vuejs.lang.typescript.service.volar.VolarTypeScriptService

internal class VueLanguageServiceProvider(project: Project) : JSLanguageServiceProvider {
  private val tsLanguageService by lazy(LazyThreadSafetyMode.NONE) { project.service<ServiceWrapper>() }
  private val volarLanguageService by lazy(LazyThreadSafetyMode.NONE) { project.service<VolarServiceWrapper>() }

  override fun getAllServices(): List<JSLanguageService> = listOf(tsLanguageService.service, volarLanguageService.service)

  override fun getService(file: VirtualFile): JSLanguageService? = allServices.firstOrNull { it.isAcceptable(file) }

  override fun isHighlightingCandidate(file: VirtualFile): Boolean {
    return TypeScriptLanguageServiceProvider.isJavaScriptOrTypeScriptFileType(file.fileType)
           || file.isDotVueFile
  }
}

@Service(Service.Level.PROJECT)
private class ServiceWrapper(project: Project) : Disposable {
  val service = VueTypeScriptService(project)

  override fun dispose() {
    Disposer.dispose(service)
  }
}

@Service(Service.Level.PROJECT)
private class VolarServiceWrapper(project: Project) : Disposable {
  val service = VolarTypeScriptService(project)

  override fun dispose() {
    Disposer.dispose(service)
  }
}