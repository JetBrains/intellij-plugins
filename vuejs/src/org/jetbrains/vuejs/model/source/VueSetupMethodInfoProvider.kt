// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.asSafely
import org.jetbrains.vuejs.codeInsight.resolveElementTo
import org.jetbrains.vuejs.index.VueFrameworkHandler
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.source.VueContainerInfoProvider.VueContainerInfo

class VueSetupMethodInfoProvider : VueContainerInfoProvider {

  override fun getInfo(descriptor: VueSourceEntityDescriptor): VueContainerInfo? =
    descriptor.source
      .asSafely<JSObjectLiteralExpression>()
      ?.let { VueCompositionInfo(it) }

  class VueCompositionInfo(val initializer: JSObjectLiteralExpression) : VueContainerInfo {

    override val provides: List<VueProvide>
      get() = rawBindings.filterIsInstance(VueProvide::class.java)

    override val injects: List<VueInject>
      get() = rawBindings.filterIsInstance(VueInject::class.java)

    private val rawBindings: List<VueNamedSymbol>
      get() = CachedValuesManager.getCachedValue(initializer) {
        CachedValueProvider.Result.create(getSetupSymbols(initializer), PsiModificationTracker.MODIFICATION_COUNT)
      }

    private fun getSetupSymbols(initializer: JSObjectLiteralExpression): List<VueNamedSymbol> =
      resolveElementTo(initializer.findProperty(SETUP_METHOD), JSFunction::class)
        ?.let { JSStubBasedPsiTreeUtil.findDescendants<JSCallExpression>(it, TokenSet.create(JSStubElementTypes.CALL_EXPRESSION)) }
        ?.mapNotNull {
          when (VueFrameworkHandler.getFunctionNameFromVueIndex(it)) {
            PROVIDE_FUN -> analyzeProvide(it)
            INJECT_FUN -> analyzeInject(it)
            else -> null
          }
        }
      ?: emptyList()

    override fun equals(other: Any?): Boolean =
      (other as? VueCompositionInfo)?.initializer == initializer

    override fun hashCode(): Int = initializer.hashCode()
  }
}

