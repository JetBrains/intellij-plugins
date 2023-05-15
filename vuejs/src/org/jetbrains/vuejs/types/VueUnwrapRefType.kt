// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.types

import com.intellij.javascript.web.js.WebJSResolveUtil.resolveSymbolFromNodeModule
import com.intellij.lang.ecmascript6.resolve.JSFileReferencesUtil
import com.intellij.lang.javascript.evaluation.JSCodeBasedTypeFactory
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeAlias
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext
import com.intellij.lang.javascript.psi.types.*
import com.intellij.lang.javascript.psi.types.JSRecordTypeImpl.PropertySignatureImpl
import com.intellij.lang.javascript.psi.types.evaluable.JSApplyCallType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.ProcessingContext
import com.intellij.util.asSafely
import org.jetbrains.vuejs.index.COMPOSITION_API_MODULE
import org.jetbrains.vuejs.index.VUE_MODULE
import org.jetbrains.vuejs.model.source.VueCompositionInfoHelper

class VueUnwrapRefType private constructor(private val typeToUnwrap: JSType, source: JSTypeSource) : JSSimpleTypeBaseImpl(source) {

  constructor (typeToUnwrap: JSType, context: PsiElement) : this(typeToUnwrap, JSTypeSourceFactory.createTypeSource(context, true))

  override fun copyWithNewSource(source: JSTypeSource): JSType =
    VueUnwrapRefType(typeToUnwrap, source)

  override fun hashCodeImpl(): Int = typeToUnwrap.hashCode()

  override fun acceptChildren(visitor: JSRecursiveTypeVisitor) {
    // We don't want to expose any children,
    // otherwise type will not be rendered in JS context.
  }

  override fun isEquivalentToWithSameClass(type: JSType, context: ProcessingContext?, allowResolve: Boolean): Boolean =
    type is VueUnwrapRefType
    && type.typeToUnwrap.isEquivalentTo(typeToUnwrap, context, allowResolve)

  override fun buildTypeTextImpl(format: JSType.TypeTextFormat, builder: JSTypeTextBuilder) {
    if (format == JSType.TypeTextFormat.SIMPLE) {
      builder.append("#VueUnwrapRefType").append("<")
      typeToUnwrap.buildTypeText(format, builder)
      builder.append(">")
      return
    }
    if (format == JSType.TypeTextFormat.PRESENTABLE &&
        ((typeToUnwrap as? JSGenericTypeImpl)?.type as? JSTypeImpl)?.typeText.equals("UnwrapNestedRefs")) {
      substituteManually().buildTypeText(format, builder)
      return
    }
    val substitute = substitute()
    if (substitute != this) {
      substitute.buildTypeText(format, builder)
    }
    else {
      builder.append(UNWRAP_REF_TYPE).append("<")
      typeToUnwrap.buildTypeText(format, builder)
      builder.append(">")
    }
  }

  override fun substituteImpl(context: JSTypeSubstitutionContext): JSType {
    val unrefFunction = resolveUnrefFunction()
    if (unrefFunction != null) {
      val baseType = JSCodeBasedTypeFactory.getPsiBasedType(unrefFunction, JSEvaluateContext(unrefFunction.containingFile))
      return JSApplyCallType(baseType, mutableListOf(typeToUnwrap), source)
    }
    else if (ApplicationManager.getApplication().isUnitTestMode) {
      assertVuePackageResolved()
    }

    // old vue-composite doesn't have unref(), see VueCompletionTest.testVue2CompositionApi
    return substituteManually()
  }

  private fun substituteManually(): JSType {
    val substituted = VueCompositionInfoHelper.substituteRefType(typeToUnwrap)
      .let {
        if (source.isJavaScript) {
          // In JS context `unknown` doesn't work properly in conditionals, replace it with `any`
          it.transformTypeHierarchy(object : JSCacheableTypeTransformerBase() {
            override fun `fun`(type: JSType): JSType =
              if (type is JSUnknownType)
                JSAnyType.get(type.source)
              else type
          })
        }
        else it
      }

    if (JSTypeUtils.isAnyType(substituted)) return JSAnyType.get(source)

    val hasUnwrap = (substituted as? JSGenericTypeImpl)
      ?.type
      ?.asSafely<JSTypeImpl>()
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
        ?.substituteUnwrapRecursively()
      return unwrapped ?: JSAnyType.get(source)
    }
    else {
      return JSGenericTypeImpl(substituted.source, unwrapRef.jsType, substituted)
        .substitute()
        .substituteUnwrapRecursively()
    }
  }

  private fun resolveUnrefFunction(): TypeScriptFunction? {
    val file = source.sourceElement?.containingFile ?: return null
    return resolveSymbolFromNodeModule(file, VUE_MODULE, "unref", TypeScriptFunction::class.java)
           ?: resolveSymbolFromNodeModule(file, COMPOSITION_API_MODULE, "unref", TypeScriptFunction::class.java)
  }

  private fun assertVuePackageResolved() {
    val resolved = let {
      val file = source.sourceElement?.containingFile ?: return@let false
      JSFileReferencesUtil.resolveModuleReference(file, VUE_MODULE).isNotEmpty()
    }
    assert(resolved) {
      "vue package is not found, make sure that test dependencies are set up (see configureVueDependencies)"
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

  private fun JSType.unwrapAliases(): JSType {
    var res = this
    while (res is JSAliasTypeImpl) {
      res = res.originalType
    }
    return res
  }

  private fun JSType.substituteUnwrapRecursively() =
    transformTypeHierarchy(object : JSCacheableTypeTransformerBase() {
      override fun `fun`(type: JSType): JSType {
        var result = type.unwrapAliases()
          .let { JSCompositeTypeImpl.optimizeTypeIfComposite(it, JSUnionOrIntersectionType.OptimizedKind.OPTIMIZED_SIMPLE) }
        // Expand only generics with "Unwrap" in the type name
        if (result is JSGenericTypeImpl
            && result.type
              .takeIf { (it is JSAliasTypeImpl || it is JSNamedType) }
              ?.getTypeText(JSType.TypeTextFormat.SIMPLE)
              ?.contains("Unwrap") == true) {
          result = result.substitute()
            .let { JSCompositeTypeImpl.optimizeTypeIfComposite(it, JSUnionOrIntersectionType.OptimizedKind.OPTIMIZED_SIMPLE) }
        }
        // In JS context `unknown` doesn't work properly in conditionals,
        // so we need to ensure that we are not left with huge list of expanded conditionals,
        // just in case there is some `unknown` out there.

        return if (result is JSRecordType) {
          result.transformTypeHierarchy {
            // JSLookupUtilImpl.isTypeAcceptableForLookupElement
            t ->
            if (t is JSGenericTypeImpl || t is JSRecordType && t != result)
              JSNamedTypeFactory.createType(StringUtil.ELLIPSIS, t.source, JSContext.STATIC)
            else t
          }
        }
        else if (result != type && !JSTypeUtils.hasTypes(result, TypeScriptConditionalTypeJSTypeImpl::class.java)) {
          result
        }
        else type
      }
    })

  companion object {

    private const val SHALLOW_UNWRAP_REF_TYPE = "ShallowUnwrapRef"
    private const val UNWRAP_REF_TYPE = "UnwrapRef"
  }

}
