package com.intellij.dts.lang.symbols

import com.intellij.dts.documentation.DtsBindingDocumentation
import com.intellij.dts.util.DtsUtil
import com.intellij.dts.zephyr.binding.DtsZephyrBinding
import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.navigation.NavigatableSymbol
import com.intellij.navigation.SymbolNavigationService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.navigation.NavigationTarget

data class DtsBindingSymbol(val binding: DtsZephyrBinding) : DtsDocumentationSymbol, NavigatableSymbol {
  override fun createPointer(): Pointer<out Symbol> = Pointer { this }

  override fun getDocumentationTarget(project: Project): DocumentationTarget {
    return DtsBindingDocumentation(project, binding)
  }

  override fun getNavigationTargets(project: Project): Collection<NavigationTarget> {
    return DtsUtil.singleResult {
      val path = binding.path ?: return@singleResult null
      val virtualFile = DtsUtil.findFile(path) ?: return@singleResult null
      val psiFile = virtualFile.findPsiFile(project) ?: return@singleResult null

      SymbolNavigationService.getInstance().psiFileNavigationTarget(psiFile)
    }
  }
}