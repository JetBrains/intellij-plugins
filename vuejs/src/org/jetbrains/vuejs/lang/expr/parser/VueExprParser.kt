// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.expr.parser

import com.intellij.psi.tree.IElementType
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser

interface VueExprParser {
    fun parseEmbeddedExpression(root: IElementType, attributeInfo: VueAttributeNameParser.VueAttributeInfo?)

    fun parseInterpolation(root: IElementType) {
      parseEmbeddedExpression(root, null)
    }

    fun parseJS(root: IElementType)
  }
