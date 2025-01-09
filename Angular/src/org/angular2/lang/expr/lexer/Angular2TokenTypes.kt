// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.lexer

import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

interface Angular2TokenTypes {
  companion object {
    @JvmField
    val ESCAPE_SEQUENCE: IElementType = Angular2TokenType("NG:ESCAPE_SEQUENCE")

    @JvmField
    val INVALID_ESCAPE_SEQUENCE: IElementType = Angular2TokenType("NG:INVALID_ESCAPE_SEQUENCE")

    @JvmField
    val XML_CHAR_ENTITY_REF: IElementType = Angular2TokenType("NG:XML_CHAR_ENTITY_REF")

    @JvmField
    val BLOCK_PARAMETER_NAME: IElementType = Angular2TokenType("NG:BLOCK_PARAMETER_NAME")

    @JvmField
    val BLOCK_PARAMETER_PREFIX: IElementType = Angular2TokenType("NG:BLOCK_PARAMETER_PREFIX")

    @JvmField
    val KEYWORDS: TokenSet = TokenSet.create(JSTokenTypes.VAR_KEYWORD, JSTokenTypes.LET_KEYWORD, JSTokenTypes.AS_KEYWORD, JSTokenTypes.NULL_KEYWORD,
                                             JSTokenTypes.UNDEFINED_KEYWORD, JSTokenTypes.TRUE_KEYWORD, JSTokenTypes.FALSE_KEYWORD,
                                             JSTokenTypes.IF_KEYWORD,
                                             JSTokenTypes.ELSE_KEYWORD, JSTokenTypes.THIS_KEYWORD, BLOCK_PARAMETER_NAME)

    @JvmField
    val STRING_PART_SPECIAL_SEQ: TokenSet = TokenSet.create(ESCAPE_SEQUENCE, INVALID_ESCAPE_SEQUENCE, XML_CHAR_ENTITY_REF)
  }
}
