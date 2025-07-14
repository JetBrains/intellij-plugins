// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.navigation.JSDeclarationEvaluator
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSPsiReferenceElement
import com.intellij.model.Pointer
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import org.jetbrains.vuejs.model.VueContainer
import org.jetbrains.vuejs.model.VueEntitiesContainer
import org.jetbrains.vuejs.model.VuePlugin

data class VueCompositionPlugin(
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

      return null
    }

    fun get(
      source: PsiElement,
    ): VueCompositionPlugin? {
      // copied form `JSGotoDeclarationHandler.getGotoDeclarationTargetsImpl`
      val declarations = when (source) {
        is ES6ImportedBinding -> JSDeclarationEvaluator.GO_TO_DECLARATION.getDeclarations(source)
        is JSPsiReferenceElement -> JSDeclarationEvaluator.GO_TO_DECLARATION.getDeclarations(source)
        else -> return null
      }

      val declaration = declarations?.singleOrNull()
                        ?: return null

      return create(declaration)
    }
  }
}
