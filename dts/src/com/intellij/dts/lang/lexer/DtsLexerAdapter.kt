package com.intellij.dts.lang.lexer

import com.intellij.dts.lang.DtsPpTokenTypes
import com.intellij.lexer.FlexAdapter
import com.intellij.dts.pp.lang.lexer.PpLexerAdapter

class DtsLexerAdapter : PpLexerAdapter(DtsPpTokenTypes, FlexAdapter(DtsLexer(null)))