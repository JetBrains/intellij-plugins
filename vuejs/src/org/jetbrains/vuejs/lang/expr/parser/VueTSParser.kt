// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.javascript.ecmascript6.parsing.TypeScriptParser
import com.intellij.psi.tree.IElementType
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.VueAttributeInfo

class VueTSParser(builder: PsiBuilder) : TypeScriptParser(builder), VueExprParser {

  //init {
  //  myExpressionParser = VueJSExpressionParser(this)
  //}

  private val extraParser = VueJSExtraParser(this, ::parseFilterOptional)

  override fun parseEmbeddedExpression(root: IElementType, attributeInfo: VueAttributeInfo?) {
    extraParser.parseEmbeddedExpression(root, attributeInfo, VueJSElementTypes.EMBEDDED_EXPR_CONTENT_TS)
  }

  private fun parseFilterOptional(): Boolean {
    if (!myExpressionParser.parseExpressionOptional()) {
      return false
    }

    return true
  }
}
