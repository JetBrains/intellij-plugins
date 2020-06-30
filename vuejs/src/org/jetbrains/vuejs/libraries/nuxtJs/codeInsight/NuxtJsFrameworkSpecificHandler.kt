// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.nuxtJs.codeInsight

import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.javascript.frameworks.JSFrameworkSpecificHandler
import com.intellij.lang.javascript.psi.JSExpectedTypeKind
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptInterface
import com.intellij.psi.PsiElement
import com.intellij.util.castSafelyTo
import org.jetbrains.vuejs.codeInsight.resolveSymbolFromNodeModule

class NuxtJsFrameworkSpecificHandler : JSFrameworkSpecificHandler {

  override fun findExpectedType(parent: PsiElement, expectedTypeKind: JSExpectedTypeKind): JSType? {
    if (parent is JSObjectLiteralExpression
        && parent.containingFile.name == "nuxt.config.js"
        && parent.parent is ES6ExportDefaultAssignment) {
      val defaultExport = resolveSymbolFromNodeModule(parent, "@nuxt/config",
                                                      "default", ES6ExportDefaultAssignment::class.java)
      return defaultExport?.stubSafeElement?.castSafelyTo<TypeScriptInterface>()?.jsType
    }
    return null
  }

}