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
import org.jetbrains.vuejs.lang.html.isVueFile
import org.jetbrains.vuejs.lang.typescript.service.classic.VueClassicTypeScriptService
import org.jetbrains.vuejs.lang.typescript.service.volar.VolarTypeScriptService

internal class VueLanguageServiceProvider(project: Project) : JSLanguageServiceProvider {
  private val classicLanguageService by lazy(LazyThreadSafetyMode.PUBLICATION) { project.service<VueClassicServiceWrapper>() }
  private val volarLanguageService by lazy(LazyThreadSafetyMode.PUBLICATION) { project.service<VolarServiceWrapper>() }

  override fun getAllServices(): List<JSLanguageService> = listOf(classicLanguageService.service, volarLanguageService.service)

  override fun getService(file: VirtualFile): JSLanguageService? = allServices.firstOrNull { it.isAcceptable(file) }

  override fun isHighlightingCandidate(file: VirtualFile): Boolean {
    return TypeScriptLanguageServiceProvider.isJavaScriptOrTypeScriptFileType(file.fileType)
           || file.isVueFile
  }
}

@Service(Service.Level.PROJECT)
private class VueClassicServiceWrapper(project: Project) : Disposable {
  val service = VueClassicTypeScriptService(project)

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