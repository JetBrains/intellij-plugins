// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.jsx.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.ecmascript6.parsing.TypeScriptParser
import com.intellij.psi.tree.IElementType
import org.jetbrains.astro.lang.jsx.AstroJsxLanguage

class AstroJsxParser(builder: PsiBuilder): TypeScriptParser(AstroJsxLanguage.INSTANCE, builder) {

  private fun parseWithRoot(root: IElementType, method: () -> Unit) {
    val rootMarker = builder.mark()
    if (builder.tokenType == JSTokenTypes.LBRACE) {
      builder.advanceLexer()
    }
    method()
    if (builder.tokenType == JSTokenTypes.RBRACE) {
      builder.advanceLexer()
    }
    rootMarker.done(root)
  }

  fun parseExpression(root: IElementType) {
    parseWithRoot(root) {
      expressionParser.parseExpression()
    }
  }

  fun parseSpreadAttribute(root: IElementType) {
    parseWithRoot(root) {
      if (builder.tokenType == JSTokenTypes.DOT_DOT_DOT) {
        builder.advanceLexer()
      }
      expressionParser.parseExpression()
    }
  }


}