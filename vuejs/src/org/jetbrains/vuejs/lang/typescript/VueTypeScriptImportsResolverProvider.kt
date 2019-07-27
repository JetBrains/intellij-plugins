// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.typescript

import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.typescript.modules.TypeScriptNodeReference
import com.intellij.lang.typescript.tsconfig.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.index.hasVue
import org.jetbrains.vuejs.lang.html.VueFileType

const val vueExtension = ".vue"
val defaultExtensionsWithDot = arrayOf(vueExtension)

class VueTypeScriptImportsResolverProvider : TypeScriptImportsResolverProvider {
  override fun isDynamicFile(project: Project, file: VirtualFile): Boolean {
    if (file.fileType != VueFileType.INSTANCE) return false

    val psiFile = PsiManager.getInstance(project).findFile(file) ?: return false
    val module = findModule(psiFile)

    return module != null && DialectDetector.isTypeScript(module)
  }

  override fun useExplicitExtension(extensionWithDot: String): Boolean = extensionWithDot == vueExtension
  override fun getExtensions(): Array<String> = defaultExtensionsWithDot

  override fun createResolver(project: Project,
                              context: TypeScriptImportResolveContext,
                              contextFile: VirtualFile): TypeScriptFileImportsResolver? {
    if (!hasVue(project)) return null

    val defaultProvider = TypeScriptImportsResolverProvider.getDefaultProvider(project, context, contextFile)
    val vueResolver = VueFileImportsResolver(project, context, typeScriptNodeResolver(project))
    return flattenAndAppendResolver(defaultProvider, vueResolver)
  }

  override fun createResolver(project: Project, config: TypeScriptConfig): TypeScriptFileImportsResolver? {
    if (!hasVue(project)) return null

    val defaultProvider = TypeScriptImportsResolverProvider.getDefaultProvider(project, config)
    val nodeProcessor = typeScriptNodeResolver(project)
    val vueResolver = VueFileImportsResolver(project, config.resolveContext, nodeProcessor)
    return flattenAndAppendResolver(defaultProvider, vueResolver)
  }

  private fun flattenAndAppendResolver(defaultProvider: TypeScriptFileImportsResolver,
                                       vueResolver: VueFileImportsResolver): TypeScriptCompositeImportsResolverImpl {
    val result = mutableListOf<TypeScriptFileImportsResolver>()
    if (defaultProvider is TypeScriptCompositeImportsResolverImpl) {
      result.addAll(defaultProvider.resolvers)
    }
    else {
      result.add(defaultProvider)
    }


    result.add(vueResolver)

    return TypeScriptCompositeImportsResolverImpl(result)
  }

  private fun typeScriptNodeResolver(project: Project) =
    TypeScriptNodeReference.TypeScriptNodeModuleDirectorySearchProcessor(project)
}
