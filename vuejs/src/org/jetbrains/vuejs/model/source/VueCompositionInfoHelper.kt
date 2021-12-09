// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeAlias
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.types.*
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.castSafelyTo
import org.jetbrains.vuejs.codeInsight.resolveIfImportSpecifier
import org.jetbrains.vuejs.codeInsight.resolveSymbolFromNodeModule
import org.jetbrains.vuejs.index.COMPOSITION_API_MODULE
import org.jetbrains.vuejs.index.VUE_MODULE
import org.jetbrains.vuejs.model.*

object VueCompositionInfoHelper {

  private const val UNWRAP_REF_TYPE = "UnwrapRef"
  private const val READ_ONLY_TYPE = "ReadOnly"

  fun createRawBindings(context: PsiElement, setupType: JSType?): List<VueNamedSymbol> {
    val unwrapRef = getUnwrapRefType(context)
    return setupType
             ?.asRecordType()
             ?.properties
             ?.mapNotNull { mapSignatureToRawBinding(it, JSTypeSubstitutionContextImpl(), unwrapRef) }
           ?: emptyList()
  }

  fun getUnwrapRefType(context: PsiElement): TypeScriptTypeAlias? =
    context.containingFile.let { file ->
      CachedValuesManager.getCachedValue(file) {
        val unwrapRef = resolveSymbolFromNodeModule(file, VUE_MODULE,
                                                    UNWRAP_REF_TYPE, TypeScriptTypeAlias::class.java)
                        ?: resolveSymbolFromNodeModule(file, COMPOSITION_API_MODULE,
                                                       UNWRAP_REF_TYPE, TypeScriptTypeAlias::class.java)
                        ?: resolveSymbolFromNodeModule(
                          file, "$COMPOSITION_API_MODULE/dist/reactivity/ref",
                          UNWRAP_REF_TYPE, TypeScriptTypeAlias::class.java)
        CachedValueProvider.Result(unwrapRef, unwrapRef ?: VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
      }
    }

  fun unwrapType(type: JSType, unwrapRef: TypeScriptTypeAlias?): JSType {
    val substituted = substituteRefType(type)

    val hasUnwrap = (substituted as? JSGenericTypeImpl)
      ?.type
      ?.castSafelyTo<JSTypeImpl>()
      ?.typeText == UNWRAP_REF_TYPE
    return if (hasUnwrap || unwrapRef == null) {
      substituted
    }
    else
      JSGenericTypeImpl(substituted.source, unwrapRef.jsType, substituted)
  }

  fun getUnwrappedRefElement(element: PsiElement?, unwrapRef: TypeScriptTypeAlias?): VueImplicitElement? {
    val resolved = (element as? JSPsiNamedElementBase)?.resolveIfImportSpecifier()
    val jsType = (resolved as? JSTypeOwner)?.jsType
    val name = resolved?.name
    return if (jsType != null && name != null) {
      val unwrappedType = unwrapType(jsType, unwrapRef)
      VueImplicitElement(name, unwrappedType, resolved, JSImplicitElement.Type.Property, true)
    }
    else null
  }

  private fun substituteRefType(type: JSType, context: JSTypeSubstitutionContextImpl? = null): JSType {
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
                                       unwrapRef: TypeScriptTypeAlias?): VueNamedSymbol {
    val name = signature.memberName
    var signatureType = signature.jsType?.let { substituteRefType(it, context) }
    var isReadOnly = false
    var hasUnwrap = false
    if (signatureType is JSAliasTypeImpl) {
      signatureType = signatureType.alias
    }
    when (signatureType) {
      is JSGenericTypeImpl -> {
        when ((signatureType.type as? JSTypeImpl)?.typeText) {
          READ_ONLY_TYPE -> isReadOnly = true
          UNWRAP_REF_TYPE -> hasUnwrap = true
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