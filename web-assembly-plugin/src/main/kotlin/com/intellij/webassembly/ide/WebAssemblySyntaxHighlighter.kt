package com.intellij.webassembly.ide

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.webassembly.ide.colors.WebAssemblyColor
import com.intellij.webassembly.lang.lexer.WebAssemblyLexer
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.ALIGNEQKEY
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.BAD_TOKEN
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.BLOCKKEY
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.BLOCK_COMMENT
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.BRTABLEINSTR
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.CALLINDIRECTINSTR
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.CALLINSTR
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.CONTROLINSTR
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.CONTROLINSTR_IDX
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.DATAKEY
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.DECLAREKEY
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.ELEMDROPINSTR
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.ELEMKEY
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.ELSEKEY
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.ENDKEY
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.EXPORTKEY
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.EXTERNKEY
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.FCONST
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.FLOAT
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.FUNCKEY
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.GLOBALINSTR
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.GLOBALKEY
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.ICONST
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.IDENTIFIER
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.IFKEY
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.IMPORTKEY
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.ITEMKEY
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.LINE_COMMENT
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.LOCALINSTR
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.LOCALKEY
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.LOOPKEY
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.LPAR
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.MEMORYINSTR
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.MEMORYINSTR_IDX
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.MEMORYINSTR_MEMARG
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.MEMORYKEY
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.MODULEKEY
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.MUTKEY
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.NUMERICINSTR
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.OFFSETEQKEY
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.OFFSETKEY
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.PARAMETRICINSTR
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.PARAMKEY
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.REFFUNCINSTR
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.REFISNULLINST
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.REFNULLINSTR
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.REFTYPE
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.RESULTKEY
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.RPAR
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.SIGNED
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.STARTKEY
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.STRING
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.TABLECOPYINSTR
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.TABLEINITINSTR
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.TABLEINSTR_IDX
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.TABLEKEY
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.THENKEY
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.TYPEKEY
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.UNSIGNED
import com.intellij.webassembly.lang.psi.WebAssemblyTypes.VALTYPE

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