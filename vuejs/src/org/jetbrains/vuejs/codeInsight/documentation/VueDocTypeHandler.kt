// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.codeInsight.documentation

import com.intellij.lang.javascript.documentation.JSDocTypeHandler
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.types.JSGenericTypeImpl
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.codeInsight.getPropTypeFromGenericType

class VueDocTypeHandler : JSDocTypeHandler {
  override fun adjustDocType(type: JSType, element: PsiElement?): JSType {
    if (type is JSGenericTypeImpl && type.isJavaScript && element is JSProperty) {
      val propType = getPropTypeFromGenericType(type)
      if (propType != null) {
        return propType
      }
    }

    return type
  }
}