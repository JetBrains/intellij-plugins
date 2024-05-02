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

%state IN_QUOTE

%%

"//".*     { return TestTypes.COMMENT; }

"#".*      { return TestTypes.PP_STATEMENT_MARKER; }

[ \n]+     { return TokenType.WHITE_SPACE; }

[a-zA-z]+  { return TestTypes.WORD; }

"."        { return TestTypes.DOT; }

<YYINITIAL> "<" { yybegin(IN_QUOTE); return TestTypes.QUOTE_START; }
<IN_QUOTE>  ">" { yybegin(YYINITIAL); return TestTypes.QUOTE_END; }
