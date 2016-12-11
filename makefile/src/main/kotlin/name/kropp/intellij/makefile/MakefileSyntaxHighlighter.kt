package name.kropp.intellij.makefile

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.psi.TokenType
import name.kropp.intellij.makefile.psi.MakefileTypes

class MakefileSyntaxHighlighter : SyntaxHighlighterBase() {
  companion object {
    val COMMENT = createTextAttributesKey("MAKEFILE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
    val TARGET = createTextAttributesKey("MAKEFILE_TARGET", DefaultLanguageHighlighterColors.CLASS_NAME)
    val SEPARATOR = createTextAttributesKey("MAKEFILE_SEPARATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
    val DEPENDENCY = createTextAttributesKey("MAKEFILE_DEPENDENCY", DefaultLanguageHighlighterColors.INSTANCE_METHOD)
    val VARIABLE_NAME = createTextAttributesKey("MAKEFILE_VARIABLE_NAME", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE)
    val VARIABLE_VALUE = createTextAttributesKey("MAKEFILE_VARIABLE_VALUE", DefaultLanguageHighlighterColors.STRING)
    val BAD_CHARACTER = createTextAttributesKey("MAKEFILE_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)

    private val BAD_CHAR_KEYS = arrayOf(BAD_CHARACTER)
    private val SEPARATOR_KEYS = arrayOf(SEPARATOR)
    private val TARGET_KEYS = arrayOf(TARGET)
    private val DEPENDENCY_KEYS = arrayOf(DEPENDENCY)
    private val VARIABLE_NAME_KEYS = arrayOf(VARIABLE_NAME)
    private val VARIABLE_VALUE_KEYS = arrayOf(VARIABLE_VALUE)
    private val COMMENT_KEYS = arrayOf(COMMENT)
    private val EMPTY_KEYS = emptyArray<TextAttributesKey>()
  }

  override fun getTokenHighlights(tokenType: IElementType) = when(tokenType) {
    MakefileTypes.COMMENT -> COMMENT_KEYS
    MakefileTypes.TARGET -> TARGET_KEYS
    //MakefileTypes.SEPARATOR -> SEPARATOR_KEYS
    MakefileTypes.DEPENDENCY -> DEPENDENCY_KEYS
    MakefileTypes.VARIABLE_NAME -> VARIABLE_NAME_KEYS
    MakefileTypes.VARIABLE_VALUE -> VARIABLE_VALUE_KEYS
    TokenType.BAD_CHARACTER -> BAD_CHAR_KEYS
    else -> EMPTY_KEYS
  }

  override fun getHighlightingLexer() = MakefileLexerAdapter()
}