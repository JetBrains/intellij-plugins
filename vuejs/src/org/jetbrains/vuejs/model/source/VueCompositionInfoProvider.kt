// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.types.JSGenericTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeSubstitutionContextImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.castSafelyTo
import org.jetbrains.vuejs.model.VueComputedProperty
import org.jetbrains.vuejs.model.VueDataProperty
import org.jetbrains.vuejs.model.VueMethod
import org.jetbrains.vuejs.model.VueNamedSymbol
import org.jetbrains.vuejs.model.source.VueContainerInfoProvider.VueContainerInfo

class VueCompositionInfoProvider : VueContainerInfoProvider {

  override fun getInfo(initializer: JSObjectLiteralExpression?, clazz: JSClass?): VueContainerInfo? {
    return initializer?.let { VueCompositionInfo(it) }
  }

  class VueCompositionInfo(val initializer: JSObjectLiteralExpression) : VueContainerInfo {

    override val computed: List<VueComputedProperty>
      get() = rawBindings.filterIsInstance(VueComputedProperty::class.java)

    override val data: List<VueDataProperty>
      get() = rawBindings.filterIsInstance(VueDataProperty::class.java)

    override val methods: List<VueMethod>
      get() = rawBindings.filterIsInstance(VueMethod::class.java)

    private val rawBindings: List<VueNamedSymbol>
      get() {
        return CachedValuesManager.getCachedValue(initializer) {
          val context = JSTypeSubstitutionContextImpl()
          CachedValueProvider.Result.create(
            initializer.findProperty("setup")
              ?.castSafelyTo<JSFunctionProperty>()
              ?.returnType
              ?.asRecordType()
              ?.properties
              ?.mapNotNull { mapSignatureToRawBinding(it, context) }
            ?: emptyList(), initializer)
        }
      }

    private fun mapSignatureToRawBinding(signature: JSRecordType.PropertySignature,
                                         context: JSTypeSubstitutionContextImpl): VueNamedSymbol? {
      val name = signature.memberName
      when (val signatureType = signature.jsType?.substitute(context)) {
        is JSGenericTypeImpl -> {
          var curType = signatureType
          var isReadOnly = false
          var isRef = false
          while (curType is JSGenericTypeImpl) {
            when((curType.type as? JSTypeImpl)?.typeText) {
              "Ref", "UnwrapRef" -> {
                isRef = true
                curType = curType.arguments.getOrNull(0)
              }
              "ReadOnly" -> {
                isReadOnly = true
                curType = curType.arguments.getOrNull(0)
              }
            }
          }
          if (isReadOnly) {
            return VueComposedComputedProperty(name, signature.memberSource.singleElement, curType)
          }
          if (isRef) {
            return VueComposedDataProperty(name, signature.memberSource.singleElement, curType)
          }
        }
        is JSFunctionType -> {
          return VueComposedMethod(name, signature.memberSource.singleElement)
        }
      }
      return VueComposedDataProperty(name, signature.memberSource.singleElement, null)
    }

    override fun equals(other: Any?): Boolean {
      return (other as? VueCompositionInfo)?.initializer == initializer
    }

    override fun hashCode(): Int {
      return initializer.hashCode()
    }
  }

  private class VueComposedDataProperty(override val name: String,
                                        override val source: PsiElement?,
                                        override val jsType: JSType?) : VueDataProperty

  private class VueComposedComputedProperty(override val name: String,
                                            override val source: PsiElement?,
                                            override val jsType: JSType?) : VueComputedProperty

  private class VueComposedMethod(override val name: String,
                                  override val source: PsiElement?): VueMethod
}
