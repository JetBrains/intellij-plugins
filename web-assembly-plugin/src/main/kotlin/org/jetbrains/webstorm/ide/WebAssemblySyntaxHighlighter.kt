package org.jetbrains.webstorm.ide

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import org.jetbrains.webstorm.ide.colors.WebAssemblyColor
import org.jetbrains.webstorm.lang.lexer.WebAssemblyLexer
import org.jetbrains.webstorm.lang.psi.WebAssemblyTypes.*

class WebAssemblySyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer(): Lexer = WebAssemblyLexer()

    override fun getTokenHighlights(tokenType: IElementType): Array<out TextAttributesKey> =
        pack(tokenMap[tokenType]?.textAttributesKey)

    private val tokenMap: Map<IElementType, WebAssemblyColor> =
        hashMapOf(
            // comments
            LINE_COMMENT to WebAssemblyColor.COMMENT,
            BLOCK_COMMENT to WebAssemblyColor.COMMENT,

            // module & modulefields
            MODULEKEY to WebAssemblyColor.KEYWORD,

            TYPEKEY to WebAssemblyColor.KEYWORD,
            IMPORTKEY to WebAssemblyColor.KEYWORD,
            FUNCKEY to WebAssemblyColor.KEYWORD,
            EXTERNKEY to WebAssemblyColor.KEYWORD,
            TABLEKEY to WebAssemblyColor.KEYWORD,
            MEMORYKEY to WebAssemblyColor.KEYWORD,
            GLOBALKEY to WebAssemblyColor.KEYWORD,
            EXPORTKEY to WebAssemblyColor.KEYWORD,
            STARTKEY to WebAssemblyColor.KEYWORD,
            ELEMKEY to WebAssemblyColor.KEYWORD,
            OFFSETKEY to WebAssemblyColor.KEYWORD,
            DECLAREKEY to WebAssemblyColor.KEYWORD,
            REFTYPE to WebAssemblyColor.KEYWORD,
            ITEMKEY to WebAssemblyColor.KEYWORD,
            DATAKEY to WebAssemblyColor.KEYWORD,

            PARAMKEY to WebAssemblyColor.KEYWORD,
            RESULTKEY to WebAssemblyColor.KEYWORD,
            MUTKEY to WebAssemblyColor.KEYWORD,
            LOCALKEY to WebAssemblyColor.KEYWORD,
            BLOCKKEY to WebAssemblyColor.KEYWORD,
            LOOPKEY to WebAssemblyColor.KEYWORD,
            ENDKEY to WebAssemblyColor.KEYWORD,
            IFKEY to WebAssemblyColor.KEYWORD,
            THENKEY to WebAssemblyColor.KEYWORD,
            ELSEKEY to WebAssemblyColor.KEYWORD,
            OFFSETEQKEY to WebAssemblyColor.KEYWORD,
            ALIGNEQKEY to WebAssemblyColor.KEYWORD,
            VALTYPE to WebAssemblyColor.KEYWORD,

            // instructions
            CONTROLINSTR to WebAssemblyColor.RESERVED,
            CONTROLINSTR_IDX to WebAssemblyColor.RESERVED,
            CALLINSTR to WebAssemblyColor.RESERVED,
            CONTROLINSTR_IDX to WebAssemblyColor.RESERVED,
            BRTABLEINSTR to WebAssemblyColor.RESERVED,
            CALLINDIRECTINSTR to WebAssemblyColor.RESERVED,
            REFISNULLINST to WebAssemblyColor.RESERVED,
            REFNULLINSTR to WebAssemblyColor.RESERVED,
            REFFUNCINSTR to WebAssemblyColor.RESERVED,
            PARAMETRICINSTR to WebAssemblyColor.RESERVED,
            LOCALINSTR to WebAssemblyColor.RESERVED,
            GLOBALINSTR to WebAssemblyColor.RESERVED,
            TABLEINSTR_IDX to WebAssemblyColor.RESERVED,
            TABLECOPYINSTR to WebAssemblyColor.RESERVED,
            TABLEINITINSTR to WebAssemblyColor.RESERVED,
            ELEMDROPINSTR to WebAssemblyColor.RESERVED,
            MEMORYINSTR to WebAssemblyColor.RESERVED,
            MEMORYINSTR_IDX to WebAssemblyColor.RESERVED,
            MEMORYINSTR_MEMARG to WebAssemblyColor.RESERVED,
            ICONST to WebAssemblyColor.RESERVED,
            FCONST to WebAssemblyColor.RESERVED,
            NUMERICINSTR to WebAssemblyColor.RESERVED,

            // other tokens
            UNSIGNED to WebAssemblyColor.NUMBER,
            SIGNED to WebAssemblyColor.NUMBER,
            FLOAT to WebAssemblyColor.NUMBER,

            STRING to WebAssemblyColor.STRING,

            IDENTIFIER to WebAssemblyColor.IDENTIFIER,

            LPAR to WebAssemblyColor.PARENTHESES,
            RPAR to WebAssemblyColor.PARENTHESES,

            BAD_TOKEN to WebAssemblyColor.BAD_CHARACTER,
            TokenType.BAD_CHARACTER to WebAssemblyColor.BAD_CHARACTER)
}