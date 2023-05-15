package org.jetbrains.webstorm.lang.parser;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static org.jetbrains.webstorm.lang.psi.WebAssemblyTypes.*;

%%

%{
  public _WebAssemblyLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class _WebAssemblyLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

EOL=\R
WHITE_SPACE=\s+


%%
<YYINITIAL> {
  {WHITE_SPACE}           { return WHITE_SPACE; }

  "RPAR"                  { return RPAR; }
  "LPAR"                  { return LPAR; }
  "MODULEKEY"             { return MODULEKEY; }
  "IDENTIFIER"            { return IDENTIFIER; }
  "UNSIGNED"              { return UNSIGNED; }
  "NUMTYPE"               { return NUMTYPE; }
  "REFTYPE"               { return REFTYPE; }
  "FUNCKEY"               { return FUNCKEY; }
  "PARAMKEY"              { return PARAMKEY; }
  "RESULTKEY"             { return RESULTKEY; }
  "MUTKEY"                { return MUTKEY; }
  "TYPEKEY"               { return TYPEKEY; }
  "BLOCKKEY"              { return BLOCKKEY; }
  "LOOPKEY"               { return LOOPKEY; }
  "IFKEY"                 { return IFKEY; }
  "THENKEY"               { return THENKEY; }
  "ELSEKEY"               { return ELSEKEY; }
  "ENDKEY"                { return ENDKEY; }
  "CONTROLINSTR"          { return CONTROLINSTR; }
  "CONTROLINSTR_IDX"      { return CONTROLINSTR_IDX; }
  "BRTABLEINSTR"          { return BRTABLEINSTR; }
  "REFISNULLINST"         { return REFISNULLINST; }
  "REFNULLINSTR"          { return REFNULLINSTR; }
  "EXTERNKEY"             { return EXTERNKEY; }
  "PARAMETRICINSTR"       { return PARAMETRICINSTR; }
  "MEMORYINSTR"           { return MEMORYINSTR; }
  "MEMORYINSTR_MEMARG"    { return MEMORYINSTR_MEMARG; }
  "ICONST"                { return ICONST; }
  "SIGNED"                { return SIGNED; }
  "FCONST"                { return FCONST; }
  "FLOAT"                 { return FLOAT; }
  "NUMERICINSTR"          { return NUMERICINSTR; }
  "CALLINSTR"             { return CALLINSTR; }
  "CALLINDIRECTINSTR"     { return CALLINDIRECTINSTR; }
  "REFFUNCINSTR"          { return REFFUNCINSTR; }
  "LOCALINSTR"            { return LOCALINSTR; }
  "GLOBALINSTR"           { return GLOBALINSTR; }
  "TABLEINSTR_IDX"        { return TABLEINSTR_IDX; }
  "TABLECOPYINSTR"        { return TABLECOPYINSTR; }
  "TABLEINITINSTR"        { return TABLEINITINSTR; }
  "ELEMDROPINSTR"         { return ELEMDROPINSTR; }
  "MEMORYINSTR_IDX"       { return MEMORYINSTR_IDX; }
  "OFFSETEQKEY"           { return OFFSETEQKEY; }
  "ALIGNEQKEY"            { return ALIGNEQKEY; }
  "IMPORTKEY"             { return IMPORTKEY; }
  "STRING"                { return STRING; }
  "TABLEKEY"              { return TABLEKEY; }
  "MEMORYKEY"             { return MEMORYKEY; }
  "GLOBALKEY"             { return GLOBALKEY; }
  "LOCALKEY"              { return LOCALKEY; }
  "EXPORTKEY"             { return EXPORTKEY; }
  "STARTKEY"              { return STARTKEY; }
  "ELEMKEY"               { return ELEMKEY; }
  "OFFSETKEY"             { return OFFSETKEY; }
  "DECLAREKEY"            { return DECLAREKEY; }
  "ITEMKEY"               { return ITEMKEY; }
  "DATAKEY"               { return DATAKEY; }
  "LINE_COMMENT"          { return LINE_COMMENT; }
  "BLOCK_COMMENT"         { return BLOCK_COMMENT; }
  "BAD_TOKEN"             { return BAD_TOKEN; }


}

[^] { return BAD_CHARACTER; }
