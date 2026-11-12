// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service

import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.dialects.TypeScriptLanguageDialect
import com.intellij.lang.typescript.lsp.TypeScriptContentMapperContribution
import com.intellij.lang.typescript.lsp.TypeScriptGoContentMappedFileSupport
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.expr.VueTSLanguage
import org.jetbrains.vuejs.lang.html.hasVueFileType

/**
 * Gives `.vue` files to TypeScript 7 as they are. TypeScript 7 maps them with `@vue/content-mapper`.
 */
class VueTypeScriptGoContentMappedFileSupport(
  private val project: Project,
) : TypeScriptGoContentMappedFileSupport {

  override fun isHighlightingCandidate(file: VirtualFile): Boolean =
    file.isInLocalFileSystem
    && file.hasVueFileType

  override fun isAcceptable(file: VirtualFile): Boolean =
    file.isInLocalFileSystem
    && file.hasVueFileType
    && isVueContext(file, project)

  override fun supportsInjectedFile(file: PsiFile): Boolean =
    file.language is VueJSLanguage
    || file.language is VueTSLanguage

  override fun supportsTypeEvaluation(
    virtualFile: VirtualFile,
    element: PsiElement,
  ): Boolean =
    when (element.language) {
      // setup
      TypeScriptLanguageDialect,
      JavascriptLanguage,
        -> element !is LeafPsiElement

      // interpolations
      VueTSLanguage,
      VueJSLanguage,
        -> element !is LeafPsiElement

      else -> false
    }

  override fun getInferredProjectContentMappers(): List<TypeScriptContentMapperContribution> =
    listOfNotNull(VueContentMapperContribution.create(project))
}