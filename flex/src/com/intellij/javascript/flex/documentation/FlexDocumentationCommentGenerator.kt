// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.documentation

import com.intellij.lang.javascript.documentation.JSDocumentationCommentGeneratorBase
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils
import com.intellij.lang.javascript.psi.types.primitives.JSVoidType


class FlexDocumentationCommentGenerator : JSDocumentationCommentGeneratorBase() {

  override fun appendFunctionInfoDoc(function: JSFunction) {
    val type: JSType? = JSPsiImplUtils.getTypeFromDeclaration(function)

    if (type != null && (type !is JSVoidType) && !function.isGetProperty()) {
      append("* @return \n")
    }
  }
}