package com.intellij.dts.lang.symbols

import com.intellij.dts.documentation.DtsBindingDocumentation
import com.intellij.dts.util.DtsUtil
import com.intellij.dts.zephyr.binding.DtsZephyrBinding
import com.intellij.dts.zephyr.binding.DtsZephyrBindingProvider
import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.navigation.NavigatableSymbol
import com.intellij.navigation.SymbolNavigationService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.navigation.NavigationTarget

data class DtsBindingSymbol(val compatible: String) : DtsDocumentationSymbol, NavigatableSymbol {
  override fun createPointer(): Pointer<out Symbol> = Pointer { this }

  private fun getBinding(project: Project): DtsZephyrBinding? = DtsZephyrBindingProvider.bindingFor(project, compatible)

  override fun getDocumentationTarget(project: Project): DocumentationTarget? {
    val binding = getBinding(project) ?: return null
    return DtsBindingDocumentation(project, binding)
  }

  override fun getNavigationTargets(project: Project): Collection<NavigationTarget> {
    return DtsUtil.singleResult {
      val path = getBinding(project)?.path ?: return@singleResult null
      val virtualFile = DtsUtil.findFile(path) ?: return@singleResult null
      val psiFile = virtualFile.findPsiFile(project) ?: return@singleResult null

      SymbolNavigationService.getInstance().psiFileNavigationTarget(psiFile)
    }
  }
}