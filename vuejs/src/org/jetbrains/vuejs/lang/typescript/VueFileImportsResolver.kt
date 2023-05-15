// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.typescript

import com.intellij.lang.javascript.config.JSImportResolveContext
import com.intellij.lang.typescript.tsconfig.TypeScriptFileImportsResolver.JS_DEFAULT_PRIORITY
import com.intellij.lang.typescript.tsconfig.TypeScriptFileImportsResolverImpl
import com.intellij.lang.typescript.tsconfig.TypeScriptImportsResolverProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import org.jetbrains.vuejs.index.VUE_DEFAULT_EXTENSIONS_WITH_DOT
import org.jetbrains.vuejs.index.VUE_MODULE
import org.jetbrains.vuejs.lang.html.VueFileType

class VueFileImportsResolver(project: Project,
                             resolveContext: JSImportResolveContext,
                             private val contextFile: VirtualFile) :
  TypeScriptFileImportsResolverImpl(project, resolveContext, VUE_DEFAULT_EXTENSIONS_WITH_DOT, listOf(VueFileType.INSTANCE)) {

  override fun processAllFilesInScope(includeScope: GlobalSearchScope, processor: Processor<in VirtualFile>) {
    if (includeScope == GlobalSearchScope.EMPTY_SCOPE) return

    //accept all, even without lang="ts"
    super.processAllFilesInScope(includeScope, processor)

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

  override fun getPriority(): Int = JS_DEFAULT_PRIORITY
}
