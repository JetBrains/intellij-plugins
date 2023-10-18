// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.signals

import com.intellij.javascript.web.js.WebJSResolveUtil
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSTypeUtils
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeAlias
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.lang.javascript.psi.types.JSGenericTypeImpl
import com.intellij.lang.javascript.psi.types.guard.TypeScriptTypeRelations
import com.intellij.psi.PsiElement
import org.angular2.lang.Angular2LangUtil

object Angular2SignalUtils {

  const val SIGNAL_TYPE: String = "Signal"
  const val SIGNAL_FUNCTION: String = "signal"

  fun signalTypeAlias(context: PsiElement?): TypeScriptTypeAlias? =
    WebJSResolveUtil.resolveSymbolFromNodeModule(
      context, Angular2LangUtil.ANGULAR_CORE_PACKAGE, SIGNAL_TYPE,
      TypeScriptTypeAlias::class.java
    )

  fun signalFunction(context: PsiElement?): TypeScriptFunction? =
    WebJSResolveUtil.resolveSymbolFromNodeModule(
      context, Angular2LangUtil.ANGULAR_CORE_PACKAGE, SIGNAL_FUNCTION,
      TypeScriptFunction::class.java
    )

  fun supportsSignals(context: PsiElement?) =
    signalTypeAlias(context) != null

  fun isSignal(elementOrExpression: PsiElement?): Boolean {
    val signalType = signalTypeAlias(elementOrExpression)
      ?.jsType
      ?.let { JSGenericTypeImpl(it.source, it, JSAnyType.get(it.source)) }
    if (signalType != null) {
      val elementType = TypeScriptTypeRelations.expandAndOptimizeTypeRecursive(
        if (elementOrExpression is JSExpression)
          JSResolveUtil.getExpressionJSType(elementOrExpression)
        else
          JSResolveUtil.getElementJSType(elementOrExpression)
      )
      if (elementType != null
          && !JSTypeUtils.isAnyType(elementType)
          && signalType.isDirectlyAssignableType(elementType, null)) {
        return true
      }
    }
    return false
  }

}

