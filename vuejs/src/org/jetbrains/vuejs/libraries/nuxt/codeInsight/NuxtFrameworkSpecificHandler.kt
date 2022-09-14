// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.nuxt.codeInsight

import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment
import com.intellij.lang.javascript.frameworks.JSFrameworkSpecificHandler
import com.intellij.lang.javascript.psi.*
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.libraries.nuxt.NUXT_CONFIG_NAMES
import org.jetbrains.vuejs.libraries.nuxt.model.NuxtModelManager

class NuxtFrameworkSpecificHandler : JSFrameworkSpecificHandler {

  override fun findExpectedType(element: PsiElement, expectedTypeKind: JSExpectedTypeKind): JSType? {
    if (element is JSObjectLiteralExpression
        && NUXT_CONFIG_NAMES.any { it == element.containingFile.name }
        && (element.parent is ES6ExportDefaultAssignment
            || (element.parent as? JSAssignmentExpression)
              ?.lOperand?.let { it as? JSDefinitionExpression }
              ?.expression?.let { it as? JSReferenceExpression }
              ?.referenceName == "exports")) {
      return NuxtModelManager.getApplication(element)?.getNuxtConfigType(element)
    }
    return null
  }

}