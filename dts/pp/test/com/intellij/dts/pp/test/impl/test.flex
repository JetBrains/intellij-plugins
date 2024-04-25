package com.intellij.dts.pp.test.impl;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;

import com.intellij.dts.pp.test.impl.psi.TestTypes;

%%

%class TestLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}

%%

"//".* { return TestTypes.COMMENT; }

"#".*  { return TestTypes.PP_STATEMENT_MARKER; }

"\n"   { return TokenType.WHITE_SPACE; }
