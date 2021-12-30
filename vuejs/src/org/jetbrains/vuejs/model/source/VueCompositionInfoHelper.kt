// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.types.*
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.codeInsight.resolveIfImportSpecifier
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.types.VueUnwrapRefType

object VueCompositionInfoHelper {

  private const val READ_ONLY_TYPE = "ReadOnly"

  fun createRawBindings(context: PsiElement, setupType: JSType?): List<VueNamedSymbol> {
    return setupType
             ?.asRecordType()
             ?.properties
             ?.mapNotNull { mapSignatureToRawBinding(it, JSTypeSubstitutionContextImpl(), context) }
           ?: emptyList()
  }

  fun getUnwrappedRefElement(element: PsiElement?, context: PsiElement): VueImplicitElement? {
    val resolved = (element as? JSPsiNamedElementBase)?.resolveIfImportSpecifier()
    val jsType = (resolved as? JSTypeOwner)?.jsType
    val name = resolved?.name
    return if (jsType != null && name != null) {
      VueImplicitElement(name, VueUnwrapRefType(jsType, context), resolved, JSImplicitElement.Type.Property, true)
    }
    else null
  }

  internal fun substituteRefType(type: JSType, context: JSTypeSubstitutionContextImpl? = null): JSType {
    var result = if (context != null) type.substitute(context) else type.substitute()
    if (result is JSAliasTypeImpl)
      result = result.alias.let { if (context != null) it.substitute(context) else it.substitute() }
    if (result is JSContextualUnionType) {
      // Find first Ref, which is not Ref<any>
      result = result.types.find {
        it is JSGenericTypeImpl
        && (it.type as? JSTypeImpl)?.typeText == "Ref"
        && it.arguments.getOrNull(0) !is JSAnyType
      } ?: result
    }
    return result ?: type
  }

  private fun mapSignatureToRawBinding(signature: JSRecordType.PropertySignature,
                                       context: JSTypeSubstitutionContextImpl,
                                       psiContext: PsiElement): VueNamedSymbol {
    val name = signature.memberName
    var signatureType = signature.jsType?.let { substituteRefType(it, context) }
    var isReadOnly = false
    if (signatureType is JSAliasTypeImpl) {
      signatureType = signatureType.alias
    }
    when (signatureType) {
      is JSGenericTypeImpl -> {
        when ((signatureType.type as? JSTypeImpl)?.typeText) {
          READ_ONLY_TYPE -> isReadOnly = true
        }
      }
      is JSFunctionType -> {
        return VueComposedMethod(name, signature.memberSource.singleElement, signature.jsType)
      }
    }
    val type = signatureType?.let { VueUnwrapRefType(it, psiContext) }
    val source = signature.memberSource.singleElement
    val element = source?.let { VueImplicitElement(signature.memberName, type, it, JSImplicitElement.Type.Property, true) }
    return if (isReadOnly) {
      VueComposedComputedProperty(name, element, type)
    }
    else {
      VueComposedDataProperty(name, element, type)
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