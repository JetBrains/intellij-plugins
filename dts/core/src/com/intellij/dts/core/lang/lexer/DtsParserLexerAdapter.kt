package com.intellij.dts.core.lang.lexer

import com.intellij.dts.core.lang.DtsPpTokenTypes
import com.intellij.dts.pp.lang.lexer.PpHighlightingLexerAdapter
import com.intellij.dts.pp.lang.lexer.PpParserLexerAdapter
import com.intellij.lexer.FlexAdapter

class DtsParserLexerAdapter : PpParserLexerAdapter(DtsPpTokenTypes, FlexAdapter(DtsLexer(null)))

class DtsHighlightingLexerAdapter : PpHighlightingLexerAdapter(DtsPpTokenTypes, FlexAdapter(DtsLexer(null)))
