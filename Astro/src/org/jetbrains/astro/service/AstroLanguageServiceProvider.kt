// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.service

import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.languageService.TypeScriptServiceProvider
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.astro.lang.AstroFileType

internal class AstroLanguageServiceProvider(project: Project) : TypeScriptServiceProvider() {
  private val lspService by lazy(LazyThreadSafetyMode.PUBLICATION) { project.service<AstroServiceWrapper>() }
  private val tsService by lazy(LazyThreadSafetyMode.PUBLICATION) { project.service<AstroPluginTypeScriptServiceWrapper>() }

  override val allServices: List<TypeScriptService>
    get() = listOf(lspService.service, tsService.service)

  override fun isHighlightingCandidate(file: VirtualFile): Boolean =
    TypeScriptLanguageServiceUtil.isJavaScriptOrTypeScriptFileType(file.fileType)
    || file.fileType == AstroFileType
}

@Service(Service.Level.PROJECT)
private class AstroServiceWrapper(project: Project) : Disposable {
  val service = AstroLspTypeScriptService(project)

  override fun dispose() {
    Disposer.dispose(service)
  }
}

@Service(Service.Level.PROJECT)
private class AstroPluginTypeScriptServiceWrapper(project: Project) : Disposable {
  val service = AstroPluginTypeScriptService(project)

  override fun dispose() {
    Disposer.dispose(service)
  }
}