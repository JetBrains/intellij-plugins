// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.nuxt.codeInsight

import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.javascript.frameworks.JSFrameworkSpecificHandler
import com.intellij.lang.javascript.psi.*
import com.intellij.psi.PsiElement
import com.intellij.util.castSafelyTo
import org.jetbrains.vuejs.libraries.nuxt.model.NuxtModelManager
import org.jetbrains.vuejs.libraries.nuxt.NUXT_CONFIG_NAMES

class NuxtFrameworkSpecificHandler : JSFrameworkSpecificHandler {

  override fun findExpectedType(parent: PsiElement, expectedTypeKind: JSExpectedTypeKind): JSType? {
    if (parent is JSObjectLiteralExpression
        && NUXT_CONFIG_NAMES.any { it == parent.containingFile.name }
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