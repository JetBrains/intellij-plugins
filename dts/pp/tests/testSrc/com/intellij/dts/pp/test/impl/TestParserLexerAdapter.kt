package com.intellij.dts.pp.test.impl

import com.intellij.dts.pp.lang.lexer.PpParserLexerAdapter
import com.intellij.lexer.FlexAdapter

class TestParserLexerAdapter : PpParserLexerAdapter(TestPpTokenTypes, FlexAdapter(TestLexer(null)))
