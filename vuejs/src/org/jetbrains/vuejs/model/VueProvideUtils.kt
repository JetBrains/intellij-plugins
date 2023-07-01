// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.types.JSGenericTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeImpl
import com.intellij.lang.javascript.psi.types.primitives.JSSymbolType
import com.intellij.lang.javascript.psi.types.primitives.JSUndefinedType
import com.intellij.lang.javascript.psi.types.primitives.JSVoidType
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.lang.javascript.psi.util.stubSafeCallArguments
import com.intellij.lang.javascript.psi.util.stubSafeStringValue
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.util.asSafely
import org.jetbrains.vuejs.codeInsight.resolveIfImportSpecifier
import org.jetbrains.vuejs.index.VueFrameworkHandler
import org.jetbrains.vuejs.model.source.VueCallInject
import org.jetbrains.vuejs.model.source.VueSourceProvide
import org.jetbrains.vuejs.types.optionalIf


fun analyzeProvide(call: JSCallExpression): VueProvide? {
  return analyzeCall(call, ::VueSourceProvide)
}

fun analyzeInject(call: JSCallExpression): VueInject? {
  return analyzeCall(call, ::VueCallInject)
}

private fun <T : VueNamedSymbol> analyzeCall(
  call: JSCallExpression,
  factory: (name: String, source: PsiElement, symbol: PsiNamedElement?) -> T
): T? {
  val referenceName = VueFrameworkHandler.getFunctionImplicitElement(call)?.userStringData
  val literal = call.stubSafeCallArguments.getOrNull(0).asSafely<JSLiteralExpression>()
  return when {
    referenceName != null -> JSStubBasedPsiTreeUtil.resolveLocally(referenceName, call).asSafely<PsiNamedElement>()
      ?.let { factory(referenceName, call, it) }
    literal != null -> literal.stubSafeStringValue?.let { factory(it, literal, null) }
    else -> null
  }
}

fun findInjectForCall(call: JSCallExpression, component: VueComponent): VueInject? {
  val injectionKey = VueFrameworkHandler.getFunctionImplicitElement(call)?.userStringData
                     ?: call.stubSafeCallArguments.getOrNull(0).asSafely<JSLiteralExpression>()?.stubSafeStringValue
                     ?: return null
  return component.asSafely<VueContainer>()?.injects?.find { it.name == injectionKey }
}

fun evaluateInjectedType(inject: VueInject, provides: List<VueProvideEntry>): JSType? {
  if (provides.isEmpty()) return null
  val defaultValue = inject.defaultValue
  val isOptional = defaultValue == null || defaultValue is JSUndefinedType || defaultValue is JSVoidType
  return provides.asSequence().map { it.provide }.find { provide ->
    provide.injectionKey?.isEquivalentTo(inject.injectionKey) ?: (provide.name == (inject.from ?: inject.name))
  }?.jsType?.optionalIf(isOptional)
}

fun resolveInjectionSymbol(element: PsiElement?): JSFieldVariable? {
  val declaration =
    when (element) {
      is JSReferenceExpression -> element.resolve().asSafely<JSFieldVariable>()
      is JSPsiNamedElementBase -> element.resolveIfImportSpecifier()
      else -> null
    } as? JSFieldVariable ?: return null

  val symbolType = JSResolveUtil.getElementJSType(declaration)?.substitute()
  return if (isInjectionSymbolType(symbolType)) declaration else null
}

fun isInjectionSymbolType(symbolType: JSType?): Boolean =
  symbolType is JSSymbolType ||
  (symbolType is JSGenericTypeImpl && symbolType.type.asSafely<JSTypeImpl>()?.typeText == "InjectionKey")