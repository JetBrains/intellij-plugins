// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.codeInsight.documentation

import com.intellij.lang.javascript.documentation.JSDocTypeHandler
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.types.JSGenericTypeImpl
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.codeInsight.getPropTypeFromGenericType
import org.jetbrains.vuejs.context.isVueContext

class VueDocTypeHandler : JSDocTypeHandler {
  override fun adjustDocType(type: JSType, element: PsiElement?): JSType? {
    if (type is JSGenericTypeImpl &&
        element is JSImplicitElement &&
        type.isJavaScript &&
        isVueContext(element) &&
        getPropTypeFromGenericType(type) != null) {
      return element.jsType
    }

    return null
  }
}