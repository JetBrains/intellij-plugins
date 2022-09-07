// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.typescript

import com.intellij.javascript.nodejs.NodeModuleDirectorySearchProcessor
import com.intellij.lang.javascript.psi.JSExecutionScope
import com.intellij.lang.javascript.psi.stubs.TypeScriptScriptContentIndex
import com.intellij.lang.typescript.tsconfig.TypeScriptFileImportsResolverImpl
import com.intellij.lang.typescript.tsconfig.TypeScriptImportResolveContext
import com.intellij.lang.typescript.tsconfig.TypeScriptImportsResolverProvider
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.util.Processor
import org.jetbrains.vuejs.index.VUE_MODULE
import org.jetbrains.vuejs.lang.html.VueFileType

class VueFileImportsResolver(project: Project,
                             resolveContext: TypeScriptImportResolveContext,
                             nodeProcessor: NodeModuleDirectorySearchProcessor,
                             private val contextFile: VirtualFile) :
  TypeScriptFileImportsResolverImpl(project, resolveContext, nodeProcessor, defaultExtensionsWithDot, listOf(VueFileType.INSTANCE)) {

  override fun processAllFilesInScope(includeScope: GlobalSearchScope, processor: Processor<in VirtualFile>) {
    if (includeScope == GlobalSearchScope.EMPTY_SCOPE) return
    StubIndex.getInstance().processElements(
      TypeScriptScriptContentIndex.KEY, TypeScriptScriptContentIndex.DEFAULT_INDEX_KEY, project,
      includeScope, null, JSExecutionScope::class.java) {

      ProgressManager.checkCanceled()
      val virtualFile = it.containingFile.virtualFile
      if (virtualFile != null && fileTypes.contains(virtualFile.fileType)) {
        if (!processor.process(virtualFile)) return@processElements false
      }
      return@processElements true
    }

    processVuePackage(processor)
  }

  /**
   * Explicitly include a Vue package typings into the import graph for completion inside script tags.
   */
  private fun processVuePackage(processor: Processor<in VirtualFile>) {
    TypeScriptImportsResolverProvider.getDefaultProvider(project, resolveContext)
      .resolveFileModule(VUE_MODULE, contextFile)
      ?.let { processor.process(it) }
  }
}
