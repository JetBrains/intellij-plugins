package com.intellij.dts.lang.lexer

import com.intellij.lexer.FlexAdapter

class DtsLexerAdapter : PpLexerAdapter(FlexAdapter(DtsLexer(null)))