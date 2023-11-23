package com.intellij.dts.lang.lexer

import com.intellij.dts.lang.DtsPpTokenTypes
import com.intellij.dts.pp.lang.lexer.PpLexerAdapter
import com.intellij.lexer.FlexAdapter

class DtsLexerAdapter : PpLexerAdapter(DtsPpTokenTypes, FlexAdapter(DtsLexer(null)))