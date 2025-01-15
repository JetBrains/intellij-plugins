// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil

import com.intellij.lexer.LayeredLexer
import com.intellij.lexer.Lexer
import com.intellij.lexer.StringLiteralLexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
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
import org.intellij.terraform.hcl.HCLSyntaxHighlighter
import org.intellij.terraform.hil.HILElementTypes.*

open class HILSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
  override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter {
    return HILSyntaxHighlighter()
  }
}

open class HILSyntaxHighlighter : SyntaxHighlighterBase() {
  override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> = when (tokenType) {
    COMMA -> pack(TIL_COMMA)
    OP_DOT -> pack(TIL_DOT)
    DOUBLE_QUOTED_STRING -> pack(TIL_STRING)
    NUMBER -> pack(TIL_NUMBER)
    ID -> pack(TIL_IDENTIFIER)
    FOR_KEYWORD, IN_KEYWORD, ENDFOR_KEYWORD, IF_KEYWORD, ELSE_KEYWORD, ENDIF_KEYWORD -> pack(TIL_KEYWORD)
    in HILTokenTypes.TIL_BRACES -> pack(TIL_BRACES)
    in HILTokenTypes.TIL_BRACKETS -> pack(TIL_BRACKETS)
    in HILTokenTypes.TIL_PARENS -> pack(TIL_PARENS)
    in HILTokenTypes.IL_ALL_OPERATORS -> pack(TIL_OPERATOR)
    in HILTokenTypes.TIL_KEYWORDS -> pack(TIL_KEYWORD)

    TokenType.BAD_CHARACTER -> pack(HighlighterColors.BAD_CHARACTER)
    StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN -> pack(TIL_VALID_ESCAPE)
    StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN, StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN -> pack(TIL_INVALID_ESCAPE)
    else -> emptyArray()
  }

  override fun getHighlightingLexer(): Lexer {
    val layeredLexer = LayeredLexer(HILLexer())
    layeredLexer.registerSelfStoppingLayer(StringLiteralLexer('\"', DOUBLE_QUOTED_STRING, false, "/", false, false), arrayOf(DOUBLE_QUOTED_STRING), IElementType.EMPTY_ARRAY)
    return layeredLexer
  }

  companion object {
    val TEMPLATE_BACKGROUND: TextAttributesKey = TextAttributesKey.createTextAttributesKey("TERRAFORM_TEMPLATE_BACKGROUND", DefaultLanguageHighlighterColors.TEMPLATE_LANGUAGE_COLOR)

    val TIL_PARENS: TextAttributesKey = TextAttributesKey.createTextAttributesKey("TIL.PARENS", DefaultLanguageHighlighterColors.PARENTHESES)
    val TIL_BRACES: TextAttributesKey = TextAttributesKey.createTextAttributesKey("TIL.BRACES", DefaultLanguageHighlighterColors.BRACES)
    val TIL_BRACKETS: TextAttributesKey = TextAttributesKey.createTextAttributesKey("TIL.BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
    val TIL_COMMA: TextAttributesKey = TextAttributesKey.createTextAttributesKey("TIL.COMMA", DefaultLanguageHighlighterColors.COMMA)
    val TIL_DOT: TextAttributesKey = TextAttributesKey.createTextAttributesKey("TIL.DOT", DefaultLanguageHighlighterColors.DOT)
    val TIL_OPERATOR: TextAttributesKey = TextAttributesKey.createTextAttributesKey("TIL.OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
    val TIL_NUMBER: TextAttributesKey = TextAttributesKey.createTextAttributesKey("TIL.NUMBER", DefaultLanguageHighlighterColors.NUMBER)
    val TIL_STRING: TextAttributesKey = TextAttributesKey.createTextAttributesKey("TIL.STRING", DefaultLanguageHighlighterColors.STRING)
    val TIL_KEYWORD: TextAttributesKey = TextAttributesKey.createTextAttributesKey("TIL.KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)

    // Artificial element type
    val TIL_IDENTIFIER: TextAttributesKey = TextAttributesKey.createTextAttributesKey("TIL.IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER)

    // Added by annotators
    val TIL_PREDEFINED_SCOPE: TextAttributesKey = TextAttributesKey.createTextAttributesKey("TIL.PREDEFINED_SCOPE", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL)
    val TIL_RESOURCE_TYPE_REFERENCE: TextAttributesKey = TextAttributesKey.createTextAttributesKey("TIL.RESOURCE_TYPE_REFERENCE", HCLSyntaxHighlighter.HCL_BLOCK_SECOND_TYPE_KEY)
    val TIL_RESOURCE_INSTANCE_REFERENCE: TextAttributesKey = TextAttributesKey.createTextAttributesKey("TIL.RESOURCE_INSTANCE_REFERENCE", HCLSyntaxHighlighter.HCL_BLOCK_NAME_KEY)
    val TIL_PROPERTY_REFERENCE: TextAttributesKey = TextAttributesKey.createTextAttributesKey("TIL.PROPERTY_REFERENCE", HCLSyntaxHighlighter.HCL_PROPERTY_KEY)

    // String escapes
    val TIL_VALID_ESCAPE: TextAttributesKey = TextAttributesKey.createTextAttributesKey("TIL.VALID_ESCAPE", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE)
    val TIL_INVALID_ESCAPE: TextAttributesKey = TextAttributesKey.createTextAttributesKey("TIL.INVALID_ESCAPE", DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE)
  }
}
