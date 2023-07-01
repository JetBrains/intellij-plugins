// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model

import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.lang.javascript.psi.util.stubSafeCallArguments
import com.intellij.lang.javascript.psi.util.stubSafeStringValue
import com.intellij.psi.PsiNamedElement
import com.intellij.util.asSafely
import org.jetbrains.vuejs.index.VueFrameworkHandler
import org.jetbrains.vuejs.model.source.VueCallInject
import org.jetbrains.vuejs.model.source.VueSourceProvide


fun analyzeProvide(call: JSCallExpression): VueProvide? {
  val referenceName = VueFrameworkHandler.getFunctionImplicitElement(call)?.userStringData
  val literal = call.stubSafeCallArguments.getOrNull(0).asSafely<JSLiteralExpression>()
  return when {
    referenceName != null -> JSStubBasedPsiTreeUtil.resolveLocally(referenceName, call).asSafely<PsiNamedElement>()
      ?.let { VueSourceProvide(referenceName, call, it) }
    literal != null -> literal.stubSafeStringValue?.let { VueSourceProvide(it, literal) }
    else -> null
  }
}

fun analyzeInject(call: JSCallExpression): VueInject? {
  val arguments = call.stubSafeCallArguments
  val injectionKey = arguments.getOrNull(0)
  if (injectionKey is JSLiteralExpression) {
    return injectionKey.stubSafeStringValue?.let { VueCallInject(it, injectionKey) }
  }
  return null
}