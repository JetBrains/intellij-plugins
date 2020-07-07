// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.nuxt.codeInsight

import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.javascript.frameworks.JSFrameworkSpecificHandler
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.TypeScriptInterface
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeAlias
import com.intellij.psi.PsiElement
import com.intellij.util.castSafelyTo
import org.jetbrains.vuejs.codeInsight.resolveSymbolFromNodeModule
import org.jetbrains.vuejs.libraries.nuxt.NUXT_CONFIG_FILE
import org.jetbrains.vuejs.libraries.nuxt.NUXT_CONFIG_PKG
import org.jetbrains.vuejs.libraries.nuxt.NUXT_TYPES_PKG
import org.jetbrains.vuejs.libraries.nuxt.model.NuxtModelManager

class NuxtFrameworkSpecificHandler : JSFrameworkSpecificHandler {

  override fun findExpectedType(parent: PsiElement, expectedTypeKind: JSExpectedTypeKind): JSType? {
    if (parent is JSObjectLiteralExpression
        && parent.containingFile.name == NUXT_CONFIG_FILE
        && (parent.parent is ES6ExportDefaultAssignment
            || parent.parent.castSafelyTo<JSAssignmentExpression>()
              ?.lOperand?.castSafelyTo<JSDefinitionExpression>()
              ?.expression?.castSafelyTo<JSReferenceExpression>()
              ?.referenceName == "exports")) {
      return NuxtModelManager.getApplication(parent)?.getNuxtConfigType(parent)
    }
    return null
  }

}