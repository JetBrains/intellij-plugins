// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.service

import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.dialects.ECMA6LanguageDialect
import com.intellij.lang.javascript.dialects.TypeScriptLanguageDialect
import com.intellij.lang.typescript.lsp.TypeScriptContentMapperContribution
import com.intellij.lang.typescript.lsp.TypeScriptGoContentMappedFileSupport
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.astro.context.isAstroProject
import org.jetbrains.astro.lang.AstroFileType
import org.jetbrains.astro.lang.AstroLanguage
import org.jetbrains.astro.lang.frontmatter.AstroFrontmatterLanguage

/**
 * Gives `.astro` files to TypeScript 7 as they are. TypeScript 7 maps them with `@astrojs/ts-content-mapper`.
 */
class AstroTypeScriptGoContentMappedFileSupport(
  private val project: Project,
) : TypeScriptGoContentMappedFileSupport {

  override fun isHighlightingCandidate(file: VirtualFile): Boolean =
    file.isInLocalFileSystem
    && file.fileType == AstroFileType

  override fun isAcceptable(file: VirtualFile): Boolean =
    file.isInLocalFileSystem
    && file.fileType == AstroFileType
    && isAstroProject(file, project)

  override fun supportsInjectedFile(file: PsiFile): Boolean =
    file.language is AstroFrontmatterLanguage
    || file.language is AstroLanguage

  override fun supportsTypeEvaluation(
    virtualFile: VirtualFile,
    element: PsiElement,
  ): Boolean =
    when (element.language) {
      TypeScriptLanguageDialect,
      JavascriptLanguage,
      ECMA6LanguageDialect,
      is AstroFrontmatterLanguage,
      is AstroLanguage,
        -> true

      else -> false
    }

  override fun getInferredProjectContentMappers(): List<TypeScriptContentMapperContribution> =
    listOfNotNull(AstroContentMapperContribution.create(project))
}
