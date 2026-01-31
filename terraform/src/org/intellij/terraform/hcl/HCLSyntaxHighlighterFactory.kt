// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl

import com.intellij.lexer.LayeredLexer
import com.intellij.lexer.Lexer
import com.intellij.lexer.StringLiteralLexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.BLOCK_COMMENT
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.BRACES
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.BRACKETS
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.CLASS_NAME
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.CLASS_REFERENCE
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.COMMA
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.IDENTIFIER
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.INSTANCE_FIELD
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.KEYWORD
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.LINE_COMMENT
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.NUMBER
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.OPERATION_SIGN
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.STRING
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.StringEscapesTokenTypes
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

open class HCLSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
  override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter {
    return HCLSyntaxHighlighter(createHclLexer())
  }
}

class HCLSyntaxHighlighter(val lexer: HCLLexer) : SyntaxHighlighterBase() {
  override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> = when (tokenType) {
    HCLElementTypes.L_CURLY, HCLElementTypes.R_CURLY -> pack(HCL_BRACES)
    HCLElementTypes.L_BRACKET, HCLElementTypes.R_BRACKET -> pack(HCL_BRACKETS)
    HCLElementTypes.COMMA -> pack(HCL_COMMA)
    HCLElementTypes.EQUALS -> pack(HCL_OPERATION_SIGN)
    HCLElementTypes.DOUBLE_QUOTED_STRING, HCLElementTypes.SINGLE_QUOTED_STRING -> pack(HCL_STRING)
    HCLElementTypes.NUMBER -> pack(HCL_NUMBER)
    HCLElementTypes.TRUE, HCLElementTypes.FALSE, HCLElementTypes.NULL -> pack(HCL_KEYWORD)
    HCLElementTypes.LINE_C_COMMENT, HCLElementTypes.LINE_HASH_COMMENT -> pack(HCL_LINE_COMMENT)
    HCLElementTypes.BLOCK_COMMENT -> pack(HCL_BLOCK_COMMENT)
    HCLElementTypes.ID -> pack(HCL_IDENTIFIER)

    TokenType.BAD_CHARACTER -> pack(HighlighterColors.BAD_CHARACTER)
    StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN -> pack(HCL_VALID_ESCAPE)
    StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN, StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN -> pack(HCL_INVALID_ESCAPE)
    else -> emptyArray()
  }

  override fun getHighlightingLexer(): Lexer {
    val layeredLexer = LayeredLexer(lexer)

    // TODO: Use custom StringLiteralLexer with \X, \U support
    layeredLexer.registerSelfStoppingLayer(StringLiteralLexer('\"', HCLElementTypes.DOUBLE_QUOTED_STRING, false, "/vaUX", true, false), arrayOf(HCLElementTypes.DOUBLE_QUOTED_STRING), IElementType.EMPTY_ARRAY)
    layeredLexer.registerSelfStoppingLayer(StringLiteralLexer('\'', HCLElementTypes.SINGLE_QUOTED_STRING, false, "/vaUX", true, false), arrayOf(HCLElementTypes.SINGLE_QUOTED_STRING), IElementType.EMPTY_ARRAY)
    return layeredLexer
  }

  companion object {
    val HCL_BRACKETS: TextAttributesKey = TextAttributesKey.createTextAttributesKey("HCL.BRACKETS", BRACKETS)
    val HCL_BRACES: TextAttributesKey = TextAttributesKey.createTextAttributesKey("HCL.BRACES", BRACES)
    val HCL_COMMA: TextAttributesKey = TextAttributesKey.createTextAttributesKey("HCL.COMMA", COMMA)
    val HCL_OPERATION_SIGN: TextAttributesKey = TextAttributesKey.createTextAttributesKey("HCL.OPERATION_SIGN", OPERATION_SIGN)
    val HCL_NUMBER: TextAttributesKey = TextAttributesKey.createTextAttributesKey("HCL.NUMBER", NUMBER)
    val HCL_STRING: TextAttributesKey = TextAttributesKey.createTextAttributesKey("HCL.STRING", STRING)
    val HCL_KEYWORD: TextAttributesKey = TextAttributesKey.createTextAttributesKey("HCL.KEYWORD", KEYWORD)
    val HCL_LINE_COMMENT: TextAttributesKey = TextAttributesKey.createTextAttributesKey("HCL.LINE_COMMENT", LINE_COMMENT)
    val HCL_BLOCK_COMMENT: TextAttributesKey = TextAttributesKey.createTextAttributesKey("HCL.BLOCK_COMMENT", BLOCK_COMMENT)

    // Artificial element type
    val HCL_IDENTIFIER: TextAttributesKey = TextAttributesKey.createTextAttributesKey("HCL.IDENTIFIER", IDENTIFIER)

    // Added by annotators
    val HCL_PROPERTY_KEY: TextAttributesKey = TextAttributesKey.createTextAttributesKey("HCL.PROPERTY_KEY", INSTANCE_FIELD)
    val HCL_BLOCK_FIRST_TYPE_KEY: TextAttributesKey = TextAttributesKey.createTextAttributesKey("HCL.BLOCK_FIRST_TYPE_KEY", KEYWORD)
    val HCL_BLOCK_SECOND_TYPE_KEY: TextAttributesKey = TextAttributesKey.createTextAttributesKey("HCL.BLOCK_SECOND_TYPE_KEY", CLASS_REFERENCE)
    val HCL_BLOCK_OTHER_TYPES_KEY: TextAttributesKey = TextAttributesKey.createTextAttributesKey("HCL.BLOCK_TYPES_KEY", CLASS_REFERENCE)
    val HCL_BLOCK_NAME_KEY: TextAttributesKey = TextAttributesKey.createTextAttributesKey("HCL.BLOCK_NAME_KEY", CLASS_NAME)
    val HCL_BLOCK_ONLY_NAME_KEY: TextAttributesKey = TextAttributesKey.createTextAttributesKey("HCL.BLOCK_ONLY_NAME_KEY", HCL_BLOCK_FIRST_TYPE_KEY)

    // String escapes
    val HCL_VALID_ESCAPE: TextAttributesKey = TextAttributesKey.createTextAttributesKey("HCL.VALID_ESCAPE", VALID_STRING_ESCAPE)
    val HCL_INVALID_ESCAPE: TextAttributesKey = TextAttributesKey.createTextAttributesKey("HCL.INVALID_ESCAPE", INVALID_STRING_ESCAPE)
  }
}