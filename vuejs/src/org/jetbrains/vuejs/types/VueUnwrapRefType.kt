// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.types

import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeSubstitutionContext
import com.intellij.lang.javascript.psi.JSTypeTextBuilder
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeAlias
import com.intellij.lang.javascript.psi.types.*
import com.intellij.lang.javascript.psi.types.JSRecordTypeImpl.PropertySignatureImpl
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.ProcessingContext
import com.intellij.util.castSafelyTo
import org.jetbrains.vuejs.codeInsight.resolveSymbolFromNodeModule
import org.jetbrains.vuejs.index.COMPOSITION_API_MODULE
import org.jetbrains.vuejs.index.VUE_MODULE
import org.jetbrains.vuejs.model.source.VueCompositionInfoHelper

class VueUnwrapRefType private constructor(private val typeToUnwrap: JSType, source: JSTypeSource) : JSSimpleTypeBaseImpl(source) {

  constructor (typeToUnwrap: JSType, context: PsiElement) : this(typeToUnwrap, JSTypeSourceFactory.createTypeSource(context, true))

  override fun copyWithNewSource(source: JSTypeSource): JSType =
    VueUnwrapRefType(typeToUnwrap, source)

  override fun hashCodeImpl(): Int = typeToUnwrap.hashCode()

  override fun acceptChildren(visitor: JSRecursiveTypeVisitor) {
    typeToUnwrap.accept(visitor)
  }

  override fun isEquivalentToWithSameClass(type: JSType, context: ProcessingContext?, allowResolve: Boolean): Boolean =
    type is VueUnwrapRefType
    && type.typeToUnwrap.isEquivalentTo(typeToUnwrap, context, allowResolve)

  override fun buildTypeTextImpl(format: JSType.TypeTextFormat, builder: JSTypeTextBuilder) {
    if (format == JSType.TypeTextFormat.SIMPLE) {
      builder.append(SHALLOW_UNWRAP_SINGLE_REF_ARTIFICIAL_TYPE).append("<")
      typeToUnwrap.buildTypeText(format, builder)
      builder.append(">")
      return
    }
    substitute().buildTypeText(format, builder)
  }

  override fun substituteImpl(context: JSTypeSubstitutionContext): JSType {
    val substituted = VueCompositionInfoHelper.substituteRefType(typeToUnwrap)

    val hasUnwrap = (substituted as? JSGenericTypeImpl)
      ?.type
      ?.castSafelyTo<JSTypeImpl>()
      ?.typeText == UNWRAP_REF_TYPE

    if (hasUnwrap) return substituted

    val unwrapRef = getUnwrapRefType() ?: return JSAnyType.get(source)

    if (unwrapRef.name == SHALLOW_UNWRAP_REF_TYPE) {
      // We have ShallowUnwrapRef - artificially evaluate generics to unwrap single type using the original ShallowUnwrapRef type.
      val unwrapped = JSGenericTypeImpl(substituted.source,
                                        unwrapRef.jsType,
                                        JSRecordTypeImpl(source, listOf(PropertySignatureImpl("val", substituted, false, false))))
        .substitute()
        .asRecordType()
        .findPropertySignature("val")
        ?.jsType
        ?.substitute()
      return unwrapped ?: JSAnyType.get(source)
    }
    else {
      return JSGenericTypeImpl(substituted.source, unwrapRef.jsType, substituted)
    }
  }

  private fun getUnwrapRefType(): TypeScriptTypeAlias? =
    source.sourceElement?.containingFile?.let { file ->
      CachedValuesManager.getCachedValue(file) {
        val unwrapRef = resolveSymbolFromNodeModule(file, VUE_MODULE,
                                                    SHALLOW_UNWRAP_REF_TYPE, TypeScriptTypeAlias::class.java)
                        ?: resolveSymbolFromNodeModule(file, COMPOSITION_API_MODULE,
                                                       SHALLOW_UNWRAP_REF_TYPE, TypeScriptTypeAlias::class.java)
                        ?: resolveSymbolFromNodeModule(file, COMPOSITION_API_MODULE,
                                                       UNWRAP_REF_TYPE, TypeScriptTypeAlias::class.java)
                        ?: resolveSymbolFromNodeModule(
                          file, "$COMPOSITION_API_MODULE/dist/reactivity/ref",
                          UNWRAP_REF_TYPE, TypeScriptTypeAlias::class.java)
        CachedValueProvider.Result(unwrapRef, unwrapRef ?: VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
      }
    }

  companion object {

    private const val SHALLOW_UNWRAP_REF_TYPE = "ShallowUnwrapRef"
    private const val SHALLOW_UNWRAP_SINGLE_REF_ARTIFICIAL_TYPE = "ShallowUnwrapSingleRef"
    private const val UNWRAP_REF_TYPE = "UnwrapRef"
  }

}