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
import org.jetbrains.vuejs.model.VuePlugin

@ConsistentCopyVisibility
data class VueCompositionPlugin
private constructor(
  override val source: JSFunction,
) : VueCompositionContainer(),
    VuePlugin {

  override val delegate: VueContainer? = null

  override fun createPointer(): Pointer<out VueEntitiesContainer> {
    val initializerPtr = source.createSmartPointer()
    return Pointer {
      initializerPtr.dereference()?.let { VueCompositionPlugin(it) }
    }
  }

  override val parents: List<VueEntitiesContainer>
    get() = emptyList()

  override fun toString(): String {
    return "VueCompositionPlugin($source)"
  }

  companion object {
    fun create(
      source: PsiElement,
    ): VueCompositionPlugin? {
      if (source is JSFunction) {
        return VueCompositionPlugin(source)
      }

      if (source is JSVariable) {
        val install = source.initializer
                        ?.let { it as? JSObjectLiteralExpression }
                        ?.findProperty(INSTALL_FUN)
                        ?.let { it as? JSFunctionProperty }
                      ?: return null

        return VueCompositionPlugin(install)
      }

      return null
    }

    fun get(
      source: PsiElement,
    ): VueCompositionPlugin? =
      VueDeclarations.findDeclaration(source)
        ?.let(::create)
  }
}
