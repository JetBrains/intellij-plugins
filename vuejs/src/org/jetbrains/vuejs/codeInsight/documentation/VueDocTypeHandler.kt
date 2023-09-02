// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.codeInsight.documentation

import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.documentation.JSDocTypeHandler
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.types.JSGenericTypeImpl
import com.intellij.psi.PsiElement
import com.intellij.util.asSafely
import org.jetbrains.vuejs.codeInsight.getPropTypeFromGenericType
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.model.source.PROPS_PROP

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

  override fun skipTypeChecking(type: JSType, element: PsiElement): Boolean {
    return element is JSProperty &&
           DialectDetector.isJavaScript(element) &&
           element.context?.context.asSafely<JSProperty>()?.name == PROPS_PROP &&
           isVueContext(element) &&
           getPropTypeFromGenericType(type) != null
  }
}