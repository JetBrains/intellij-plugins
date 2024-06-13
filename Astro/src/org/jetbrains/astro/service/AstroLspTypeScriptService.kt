// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.service

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.lang.typescript.compiler.TypeScriptService
import com.intellij.lang.typescript.lsp.BaseLspTypeScriptService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

class AstroLspTypeScriptService(project: Project) : BaseLspTypeScriptService(project, AstroLspServerSupportProvider::class.java) {
  override val name = "Astro LSP"
  override val prefix = "Astro"

  override fun isAcceptable(file: VirtualFile): Boolean = AstroServiceSetActivationRule.isLspServerEnabledAndAvailable(project, file)

  override fun getCompletionMergeStrategy(parameters: CompletionParameters, file: PsiFile, context: PsiElement): TypeScriptService.CompletionMergeStrategy = TypeScriptService.CompletionMergeStrategy.MERGE
}