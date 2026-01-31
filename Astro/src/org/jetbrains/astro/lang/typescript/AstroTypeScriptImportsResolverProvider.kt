// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.typescript

import com.intellij.lang.javascript.config.JSImportResolveContext
import com.intellij.lang.typescript.tsconfig.TypeScriptConfig
import com.intellij.lang.typescript.tsconfig.TypeScriptFileImportsResolver
import com.intellij.lang.typescript.tsconfig.TypeScriptFileImportsResolver.JS_DEFAULT_PRIORITY
import com.intellij.lang.typescript.tsconfig.TypeScriptFileImportsResolverImpl
import com.intellij.lang.typescript.tsconfig.TypeScriptImportResolveContext
import com.intellij.lang.typescript.tsconfig.TypeScriptImportsResolverProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.astro.lang.AstroFileType

internal const val astroExtension = ".astro"
internal val astroExtensionsWithDot = arrayOf(astroExtension)

class AstroTypeScriptImportsResolverProvider : TypeScriptImportsResolverProvider {
  override fun isImplicitTypeScriptFile(project: Project, file: VirtualFile): Boolean {
    return file.fileType == AstroFileType
  }

  override fun getExtensions(): Array<String> = astroExtensionsWithDot

  override fun contributeResolver(project: Project, config: TypeScriptConfig): TypeScriptFileImportsResolver {
    return AstroFileImportsResolverImpl(project, config.resolveContext)
  }

  override fun contributeResolver(project: Project,
                                  context: TypeScriptImportResolveContext,
                                  contextFile: VirtualFile): TypeScriptFileImportsResolver? {
    if (contextFile.fileType != AstroFileType) return null
    return AstroFileImportsResolverImpl(project, context)
  }
}

class AstroFileImportsResolverImpl(project: Project, resolveContext: JSImportResolveContext)
  : TypeScriptFileImportsResolverImpl(project, resolveContext, astroExtensionsWithDot, listOf(AstroFileType)) {
  override fun getPriority(): Int = JS_DEFAULT_PRIORITY
}
