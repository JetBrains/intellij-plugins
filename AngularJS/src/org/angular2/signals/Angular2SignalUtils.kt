// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.signals

import com.intellij.javascript.web.js.WebJSResolveUtil
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeAlias
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
}

