package com.intellij.dts.lang.lexer

import com.intellij.lexer.FlexAdapter

class DtsLexerAdapter : FlexAdapter(DtsLexer(null))