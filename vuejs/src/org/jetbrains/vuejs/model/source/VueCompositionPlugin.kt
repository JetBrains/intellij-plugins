// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSFunctionProperty
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.model.Pointer
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import org.jetbrains.vuejs.model.VueContainer
import org.jetbrains.vuejs.model.VueEntitiesContainer
import org.jetbrains.vuejs.model.VueMode
import org.jetbrains.vuejs.model.VuePlugin

@ConsistentCopyVisibility
data class VueCompositionPlugin
private constructor(
  override val source: JSFunction,
  private val mode: VueMode,
) : VueCompositionContainer(mode),
    VuePlugin {

  override val delegate: VueContainer? = null

  override fun createPointer(): Pointer<out VueEntitiesContainer> {
    val sourcePointer = source.createSmartPointer()
    val mode = this.mode
    return Pointer {
      sourcePointer.dereference()
        ?.let { VueCompositionPlugin(it, mode) }
    }
  }

  override val parents: List<VueEntitiesContainer>
    get() = emptyList()

  override fun toString(): String {
    return "VueCompositionPlugin($source)"
  }

  companion object {
    private fun create(
      source: PsiElement,
      mode: VueMode,
    ): VueCompositionPlugin? {
      if (source is JSFunction) {
        return VueCompositionPlugin(source, mode)
      }

      if (source is JSVariable) {
        val install = source.initializer
                        ?.let { it as? JSObjectLiteralExpression }
                        ?.findProperty(INSTALL_FUN)
                        ?.let { it as? JSFunctionProperty }
                      ?: return null

        return VueCompositionPlugin(install, mode)
      }

      return null
    }

    fun get(
      source: PsiElement,
      mode: VueMode,
    ): VueCompositionPlugin? =
      VueDeclarations.findDeclaration(source)
        ?.let { create(it, mode) }
  }
}
