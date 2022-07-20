package com.intellij.deno.modules

import com.intellij.deno.DenoSettings
import com.intellij.deno.service.DenoTypings
import com.intellij.lang.typescript.tsconfig.TypeScriptConfig
import com.intellij.lang.typescript.tsconfig.TypeScriptFileImportsResolver
import com.intellij.lang.typescript.tsconfig.TypeScriptImportResolveContext
import com.intellij.lang.typescript.tsconfig.TypeScriptImportsResolverProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor

class DenoTypeScriptImportResolverProvider : TypeScriptImportsResolverProvider {
  override fun getExtensions(): Array<String> = emptyArray()
  override fun contributeResolver(project: Project, config: TypeScriptConfig) = createDenoTypeScriptImportResolver(project)

  override fun contributeResolver(project: Project,
                                  context: TypeScriptImportResolveContext,
                                  contextFile: VirtualFile) = createDenoTypeScriptImportResolver(project)

  private fun createDenoTypeScriptImportResolver(project: Project): TypeScriptFileImportsResolver? =
    if (!DenoSettings.getService(project).isUseDeno()) null else DenoLibTypeScriptImportResolver(project)
}

class DenoLibTypeScriptImportResolver(private val project: Project) : TypeScriptFileImportsResolver {
  override fun resolveFileModule(moduleName: String, context: VirtualFile): VirtualFile? = null
  override fun getExtensionsWithDot(): Array<String> = emptyArray()

  override fun processAllFilesInScope(includeScope: GlobalSearchScope, processor: Processor<in VirtualFile>) {
    DenoTypings.getInstance(project).getDenoTypingsVirtualFile()?.let {
      processor.process(it)
    }
  }
}