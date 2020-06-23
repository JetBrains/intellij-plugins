// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeAlias
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.types.JSAliasTypeImpl
import com.intellij.lang.javascript.psi.types.JSGenericTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeSubstitutionContextImpl
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.castSafelyTo
import org.jetbrains.vuejs.codeInsight.resolveSymbolFromNodeModule
import org.jetbrains.vuejs.index.COMPOSITION_API_MODULE
import org.jetbrains.vuejs.index.VUE_MODULE
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.source.VueContainerInfoProvider.VueContainerInfo

class VueCompositionInfoProvider : VueContainerInfoProvider {

  override fun getInfo(descriptor: VueSourceEntityDescriptor): VueContainerInfo? {
    return descriptor.initializer?.let { VueCompositionInfo(it) }
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
          val unwrapRef = resolveSymbolFromNodeModule(initializer, VUE_MODULE,
                                                      "UnwrapRef", TypeScriptTypeAlias::class.java)
                          ?: resolveSymbolFromNodeModule(
                            initializer, "$COMPOSITION_API_MODULE/dist/reactivity/ref",
                            "UnwrapRef", TypeScriptTypeAlias::class.java)
          CachedValueProvider.Result.create(
            initializer.findProperty(SETUP_METHOD)
              ?.castSafelyTo<JSFunctionProperty>()
              ?.returnType
              ?.asRecordType()
              ?.properties
              ?.mapNotNull { mapSignatureToRawBinding(it, context, unwrapRef) }
            ?: emptyList(),
            initializer, unwrapRef ?: VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
        }
      }

    private fun mapSignatureToRawBinding(signature: JSRecordType.PropertySignature,
                                         context: JSTypeSubstitutionContextImpl,
                                         unwrapRef: TypeScriptTypeAlias?): VueNamedSymbol? {
      val name = signature.memberName
      var signatureType = signature.jsType?.substitute(context)
      var isReadOnly = false
      var hasUnwrap = false
      if (signatureType is JSAliasTypeImpl) {
        signatureType = signatureType.alias
      }
      when (signatureType) {
        is JSGenericTypeImpl -> {
          when ((signatureType.type as? JSTypeImpl)?.typeText) {
            "ReadOnly" -> isReadOnly = true
            "UnwrapRef" -> hasUnwrap = true
          }
        }
        is JSFunctionType -> {
          return VueComposedMethod(name, signature.memberSource.singleElement, signature.jsType)
        }
      }
      val type = if (hasUnwrap || signatureType == null || unwrapRef == null) {
        signatureType
      }
      else {
        JSGenericTypeImpl(signatureType.source, unwrapRef.jsType, signatureType)
      }
      val source = signature.memberSource.singleElement
      val element = if (source != null) {
        VueImplicitElement(signature.memberName, type, source, JSImplicitElement.Type.Property, true)
      }
      else {
        null
      }
      return if (isReadOnly) {
        VueComposedComputedProperty(name, element, type)
      }
      else {
        VueComposedDataProperty(name, element, type)
      }
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
                                  override val source: PsiElement?,
                                  override val jsType: JSType?) : VueMethod
}
