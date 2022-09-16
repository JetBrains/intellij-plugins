// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.evaluation.JSCodeBasedTypeFactory
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext
import com.intellij.lang.javascript.psi.types.JSAsyncReturnType
import com.intellij.lang.javascript.psi.types.JSGenericTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeImpl
import com.intellij.lang.javascript.psi.types.evaluable.JSReturnedExpressionType
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.asSafely
import org.jetbrains.vuejs.codeInsight.resolveElementTo
import org.jetbrains.vuejs.model.VueComputedProperty
import org.jetbrains.vuejs.model.VueDataProperty
import org.jetbrains.vuejs.model.VueMethod
import org.jetbrains.vuejs.model.VueNamedSymbol
import org.jetbrains.vuejs.model.source.VueContainerInfoProvider.VueContainerInfo

class VueCompositionInfoProvider : VueContainerInfoProvider {

  override fun getInfo(descriptor: VueSourceEntityDescriptor): VueContainerInfo? =
    descriptor.source
      .asSafely<JSObjectLiteralExpression>()
      ?.let { VueCompositionInfo(it) }

  class VueCompositionInfo(val initializer: JSObjectLiteralExpression) : VueContainerInfo {

    override val computed: List<VueComputedProperty>
      get() = rawBindings.filterIsInstance(VueComputedProperty::class.java)

    override val data: List<VueDataProperty>
      get() = rawBindings.filterIsInstance(VueDataProperty::class.java)

    override val methods: List<VueMethod>
      get() = rawBindings.filterIsInstance(VueMethod::class.java)

    private val rawBindings: List<VueNamedSymbol>
      get() = CachedValuesManager.getCachedValue(initializer) {
        CachedValueProvider.Result.create(VueCompositionInfoHelper.createRawBindings(
          initializer, getSetupFunctionType(initializer)
        ), PsiModificationTracker.MODIFICATION_COUNT)
      }

    private fun getSetupFunctionType(initializer: JSObjectLiteralExpression): JSType? =
      resolveElementTo(initializer.findProperty(SETUP_METHOD), JSFunction::class)
        ?.returnType
        ?.let { returnType ->
          (returnType as? JSAsyncReturnType)
            ?.substitute()
            ?.asSafely<JSGenericTypeImpl>()
            ?.takeIf { (it.type as? JSTypeImpl)?.typeText == "Promise" }
            ?.arguments
            ?.getOrNull(0)
          ?: (returnType as? JSReturnedExpressionType)?.findAssociatedExpression()?.let {
            JSCodeBasedTypeFactory.getPsiBasedType(it, JSEvaluateContext(it.containingFile))
          }
          ?: returnType
        }

    override fun equals(other: Any?): Boolean =
      (other as? VueCompositionInfo)?.initializer == initializer

    override fun hashCode(): Int = initializer.hashCode()
  }
}

