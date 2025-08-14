package com.intellij.coldFusion.model.lexer;
import com.intellij.openapi.util.text.StringUtil;

import com.intellij.openapi.project.Project;
import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.CfmlUtil;
import com.intellij.util.containers.Stack;
import com.intellij.util.ArrayUtil;

%%

%{
  Project myProject;
  final CfscriptLexerConfiguration myCurrentConfiguration = new CfscriptLexerConfiguration();

  public class CfscriptLexerConfiguration {
      public int mySharpCounter = 0;
      public int myCommentCounter = 0;
      public Stack<Integer> myReturnStack = new Stack<>();

      public CfscriptLexerConfiguration() {}

      public CfscriptLexerConfiguration(int sharpCounter, int commentCounter,
                                        Stack<Integer> returnStack) {
          mySharpCounter = sharpCounter;
          myCommentCounter = commentCounter;
          myReturnStack = returnStack;
      }

      public void reset() {
          mySharpCounter = 0;
          myCommentCounter = 0;
          myReturnStack.clear();
      }
  }

  public _CfscriptLexer(Project project) {
    this((java.io.Reader)null);
    myProject = project;
  }

  private IElementType startComment(int stateToReturnTo) {
    myCurrentConfiguration.myCommentCounter = 0;
    myCurrentConfiguration.myReturnStack.push(stateToReturnTo);
    myCurrentConfiguration.myCommentCounter++;
    yybegin(COMMENT);
    return CfmlTokenTypes.COMMENT;
  }
%}

%class _CfscriptLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%caseless
%ignorecase

COMMENTSTART = "<!---"
COMMENTBEGIN = "!---"
COMMENTFINISH = "-->"

IDENTIFIER=([:jletter:] | [_]) ([:jletterdigit:] | [_])*
WHITE_SPACE_CHAR=[\ \n\r\t\f]+
WHITE_SPACE_CHAR_A=[\ \n\r\t\f]*

INTEGER = 0 | [1-9] ([0-9])*
DOUBLE = {INTEGER}"."[0-9]*
IDENTIFIER=[:jletter:] [:jletterdigit:]*
IN_KW="in"|"IN"

DOUBLEQUOTE = \"
SINGLEQUOTE = \'

SHARP_SYMBOL = #

DOUBLEQUOTED_STRING = ([^#\"] | \"\" | "##")+
SINGLEQUOTED_STRING = ([^#\'] | \'\' | "##")+
LINE_TERMINATOR = \r|\n|\r\n

MULTILINE_COMMENT = ("/*")~("*/")
ONELINECOMMENTSTART = ("//")[^\n]*
VARIABLE_TYPE_DECL = (("/*"){WHITE_SPACE_CHAR}"@cfmlvariable"~("*/"))|(("//"){WHITE_SPACE_CHAR}"@cfmlvariable"~{LINE_TERMINATOR})

%state DOUBLE_QUOTED_STRING, SINGLE_QUOTED_STRING, DOUBLEQUOTE_CLOSER, SINGLEQUOTE_CLOSER
%state EXPRESSION, X, Y
%state COMMENT, COMMENTEND

%%

<YYINITIAL> {MULTILINE_COMMENT} {return CfscriptTokenTypes.COMMENT;}
<YYINITIAL> {ONELINECOMMENTSTART}/{LINE_TERMINATOR} {return CfscriptTokenTypes.COMMENT;}
<YYINITIAL> {COMMENTSTART} {return startComment(YYINITIAL);}
<YYINITIAL> {VARIABLE_TYPE_DECL} { return CfmlTokenTypes.VAR_ANNOTATION; }

<YYINITIAL>{
 //=========================COLDFUSION 8 KEYWORDS==================================
    "break" { return CfscriptTokenTypes.BREAK_KEYWORD; }
    "abort" { return CfscriptTokenTypes.ABORT_KEYWORD; }
    "default" { return CfscriptTokenTypes.DEFAULT_KEYWORD; }
    "function" { return CfscriptTokenTypes.FUNCTION_KEYWORD; }
    "switch" { return CfscriptTokenTypes.SWITCH_KEYWORD; }
    "case" { return CfscriptTokenTypes.CASE_KEYWORD; }
    "do" { return CfscriptTokenTypes.DO_KEYWORD; }
    "if" { return CfscriptTokenTypes.IF_KEYWORD; }
    "try" { return CfscriptTokenTypes.TRY_KEYWORD; }
    "catch" { return CfscriptTokenTypes.CATCH_KEYWORD; }
    "finally" { return CfscriptTokenTypes.FINALLY_KEYWORD; }
    "rethrow" { return CfscriptTokenTypes.RETHROW_KEYWORD; }

    "else" { return CfscriptTokenTypes.ELSE_KEYWORD; }
    "var"/({WHITE_SPACE_CHAR} {IDENTIFIER}) { return CfscriptTokenTypes.VAR_KEYWORD; }

    "for" { return CfscriptTokenTypes.FOR_KEYWORD; }
    "continue" { return CfscriptTokenTypes.CONTINUE_KEYWORD; }
    "return" { return CfscriptTokenTypes.RETURN_KEYWORD; }
    "while" { return CfscriptTokenTypes.WHILE_KEYWORD; }

//=========================COLDFUSION 9 KEYWORDS==================================
    "component" { return CfscriptTokenTypes.COMPONENT_KEYWORD; }
    "interface" { return CfscriptTokenTypes.INTERFACE_KEYWORD; }

    "pageencoding" { return CfscriptTokenTypes.PAGEENCODING_KEYWORD; }

    "abort" { return CfscriptTokenTypes.ABORT_KEYWORD; }

    "include" { return CfscriptTokenTypes.INCLUDE_KEYWORD; }

    // access types
    "public" { return CfscriptTokenTypes.PUBLIC_KEYWORD; }
    "private" { return CfscriptTokenTypes.PRIVATE_KEYWORD; }
    "remote" { return CfscriptTokenTypes.REMOTE_KEYWORD; }
    "package" { return CfscriptTokenTypes.PACKAGE_KEYWORD; }

    // return types
    "public" { return CfscriptTokenTypes.PUBLIC_KEYWORD; }
    "private" { return CfscriptTokenTypes.PRIVATE_KEYWORD; }
    "remote" { return CfscriptTokenTypes.REMOTE_KEYWORD; }
    "package" { return CfscriptTokenTypes.PACKAGE_KEYWORD; }

    "required" { return CfscriptTokenTypes.REQUIRED_KEYWORD; }

    "any" { return CfscriptTokenTypes.ANY_TYPE; }
    "array" { return CfscriptTokenTypes.ARRAY_TYPE; }
    "binary" { return CfscriptTokenTypes.BINARY_TYPE; }
    "boolean" { return CfscriptTokenTypes.BOOLEAN_TYPE; }
    "date" { return CfscriptTokenTypes.DATE_TYPE; }
    "guid" { return CfscriptTokenTypes.GUID_TYPE; }
    "numeric" { return CfscriptTokenTypes.NUMERIC_TYPE; }
    /* "query" { return CfscriptTokenTypes.QUERY_TYPE; }*/
    "string" { return CfscriptTokenTypes.STRING_TYPE; }
    "struct" { return CfscriptTokenTypes.STRUCT_TYPE; }
    "uuid" { return CfscriptTokenTypes.UUID_TYPE; }
    "variableName" { return CfscriptTokenTypes.VARIABLENAME_TYPE; }
    "void" { return CfscriptTokenTypes.VOID_TYPE; }
    "xml" { return CfscriptTokenTypes.XML_TYPE; }

    "import" { return CfscriptTokenTypes.IMPORT_KEYWORD; }
    "?:" { return CfscriptTokenTypes.ELVIS; }

    /*
    "abort" { return CfscriptTokenTypes.ABORT_KEYWORD; }
    "include" { return CfscriptTokenTypes.INCLUDE_KEYWORD; }
    "location" { return CfscriptTokenTypes.LOCATION_KEYWORD; }
    "lock" { return CfscriptTokenTypes.LOCK_KEYWORD; }
    "new" { return CfscriptTokenTypes.NEW_KEYWORD; }
    "param" { return CfscriptTokenTypes.PARAM_KEYWORD; }
    "pageencoding" { return CfscriptTokenTypes.PARAM_KEYWORD; }
    "property" { return CfscriptTokenTypes.PROPERTY_KEYWORD; }
    "thread" { return CfscriptTokenTypes.THREAD_KEYWORD; }
    "throw" { return CfscriptTokenTypes.THROW_KEYWORD; }
    "trace" { return CfscriptTokenTypes.TRACE_KEYWORD; }
    "transaction" { return CfscriptTokenTypes.TRANSACTION_KEYWORD; }
    */
}

<YYINITIAL> "false" { return CfscriptTokenTypes.BOOLEAN; }
<YYINITIAL> "true" { return CfscriptTokenTypes.BOOLEAN; }

<YYINITIAL,COMMENT, COMMENTEND, EXPRESSION, X, Y> {WHITE_SPACE_CHAR} {return CfscriptTokenTypes.WHITE_SPACE; }
<YYINITIAL> "&" {return CfscriptTokenTypes.CONCAT; }
<YYINITIAL> "&=" {return CfscriptTokenTypes.CONCAT_EQ; }
<YYINITIAL> "," {return CfscriptTokenTypes.COMMA; }
<YYINITIAL> "." {return CfscriptTokenTypes.POINT; }
<YYINITIAL> "(" {return CfscriptTokenTypes.L_BRACKET; }
<YYINITIAL> ")" {return CfscriptTokenTypes.R_BRACKET; }
<YYINITIAL> "[" {return CfscriptTokenTypes.L_SQUAREBRACKET; }
<YYINITIAL> "]" {return CfscriptTokenTypes.R_SQUAREBRACKET; }
<YYINITIAL> "{" {return CfscriptTokenTypes.L_CURLYBRACKET; }
<YYINITIAL> "}" {return CfscriptTokenTypes.R_CURLYBRACKET; }
<YYINITIAL> ";" {return CfscriptTokenTypes.SEMICOLON; }
<YYINITIAL> ":" {return CfscriptTokenTypes.DOTDOT; }
<YYINITIAL> "?" {return CfscriptTokenTypes.QUESTION; }
/* arithmetic operators */
<YYINITIAL> "=" {return CfmlTokenTypes.ASSIGN; }
<YYINITIAL> "+" {return CfscriptTokenTypes.ADD; }
<YYINITIAL> "+=" {return CfscriptTokenTypes.ADD_EQ; }
<YYINITIAL> "-" {return CfscriptTokenTypes.MINUS; }
<YYINITIAL> "-=" {return CfscriptTokenTypes.MINUS_EQ; }
<YYINITIAL> "*" {return CfscriptTokenTypes.MUL; }
<YYINITIAL> "*=" {return CfscriptTokenTypes.MUL_EQ; }
<YYINITIAL> "/" {return CfscriptTokenTypes.DEV; }
<YYINITIAL> "/=" {return CfscriptTokenTypes.DEV_EQ; }
<YYINITIAL> "++" {return CfscriptTokenTypes.INC; }
<YYINITIAL> "--" {return CfscriptTokenTypes.DEC; }
<YYINITIAL> "%" {return CfscriptTokenTypes.MOD; }
<YYINITIAL> "\\" {return CfscriptTokenTypes.INT_DEV; }
<YYINITIAL> "^" {return CfscriptTokenTypes.POW; }
/* logic operators */
<YYINITIAL> "!" {return CfscriptTokenTypes.NOT; }
<YYINITIAL> "||" {return CfscriptTokenTypes.OR; }
<YYINITIAL> "&&" {return CfscriptTokenTypes.AND; }

<YYINITIAL> "==" {return CfscriptTokenTypes.EQEQ; }
<YYINITIAL> "!=" {return CfscriptTokenTypes.NEQ; }
<YYINITIAL> "<" {return CfscriptTokenTypes.LT; }
<YYINITIAL> "<=" {return CfscriptTokenTypes.LTE; }
<YYINITIAL> ">" {return CfscriptTokenTypes.GT; }
<YYINITIAL> ">=" {return CfscriptTokenTypes.GTE; }

<YYINITIAL> "in" | "IN" { return CfscriptTokenTypes.IN_L; }
<YYINITIAL> "MOD" {return CfscriptTokenTypes.MOD_L; }
<YYINITIAL> "NOT" | "not" {return CfscriptTokenTypes.NOT_L; }
<YYINITIAL> "AND" | "and" {return CfscriptTokenTypes.AND_L; }
<YYINITIAL> "OR" | "or" {return CfscriptTokenTypes.OR_L; }
<YYINITIAL> "XOR" | "xor" {return CfscriptTokenTypes.XOR_L; }
<YYINITIAL> "EQV" | "eqv" {return CfscriptTokenTypes.NOT_XOR_L; }
<YYINITIAL> "IMP" | "imp" {return CfscriptTokenTypes.IMP_L; }
<YYINITIAL> "IS" | "EQUAL" | "EQ" | "is" | "equal" | "eq" {return CfscriptTokenTypes.EQ_L; }
<YYINITIAL> "IS NOT" | "NOT EQUAL" | "NEQ" | "is not" | "not equals" | "neq" {return CfscriptTokenTypes.NEQ_L; }
<YYINITIAL> "CONTAINS" | "contains" {return CfscriptTokenTypes.CONTAINS_L; }
<YYINITIAL> "DOES NOT CONTAIN" | "does not contain" {return CfscriptTokenTypes.NOT_CONTAINS_L; }
<YYINITIAL> "GREATER THAN" | "GT" | "greater than" | "gt" {return CfscriptTokenTypes.GT_L; }
<YYINITIAL> "LESS THAN" | "LT" | "less than" | "lt" {return CfscriptTokenTypes.LT_L; }
<YYINITIAL> "GREATER THAN OR EQUAL TO" | "GTE" | "GE" | "greater than or equal to" | "gte" | "ge" {return CfscriptTokenTypes.GE_L; }
<YYINITIAL> "LESS THAN OR EQUAL TO" | "LTE" | "LE" | "less than or equal to" | "lte" | "le" {return CfscriptTokenTypes.LE_L; }

/* numbers */
<YYINITIAL> {INTEGER} {return CfscriptTokenTypes.INTEGER; }
<YYINITIAL> {DOUBLE} {return CfscriptTokenTypes.DOUBLE; }
/* strings */
/*<YYINITIAL> {IDENTIFIER}/("(")  { return CfscriptTokenTypes.FUNCTION; }*/
<YYINITIAL> {IDENTIFIER} / (".")  {
    if (ArrayUtil.find(CfmlUtil.getVariableScopes(myProject), StringUtil.toLowerCase(yytext().toString())) != -1) {
        return CfscriptTokenTypes.SCOPE_KEYWORD;
    } else {
        return CfscriptTokenTypes.IDENTIFIER;
    }
 }
<YYINITIAL> {IDENTIFIER} { return CfscriptTokenTypes.IDENTIFIER; }

<YYINITIAL> {SINGLEQUOTE} {
    yybegin(SINGLE_QUOTED_STRING);
    return CfmlTokenTypes.SINGLE_QUOTE;
}
<YYINITIAL> {DOUBLEQUOTE} {
    yybegin(DOUBLE_QUOTED_STRING);
    return CfmlTokenTypes.DOUBLE_QUOTE;
}
<YYINITIAL> {SHARP_SYMBOL} {
    if (myCurrentConfiguration.mySharpCounter == 0) {
        myCurrentConfiguration.myReturnStack.push(YYINITIAL);
        myCurrentConfiguration.mySharpCounter = 1;
        return CfscriptTokenTypes.OPENSHARP;
    }
    myCurrentConfiguration.mySharpCounter--;
    yybegin(myCurrentConfiguration.myReturnStack.pop());
    return CfscriptTokenTypes.CLOSESHARP;
}

<DOUBLE_QUOTED_STRING> {DOUBLEQUOTED_STRING} {yybegin(DOUBLEQUOTE_CLOSER); return CfmlTokenTypes.STRING_TEXT;}
<DOUBLE_QUOTED_STRING> {DOUBLEQUOTED_STRING}/{SHARP_SYMBOL} {return CfmlTokenTypes.STRING_TEXT;}
<DOUBLE_QUOTED_STRING> {SHARP_SYMBOL} {
    myCurrentConfiguration.myReturnStack.push(DOUBLE_QUOTED_STRING);
    myCurrentConfiguration.mySharpCounter++;
    yybegin(YYINITIAL);
    return CfscriptTokenTypes.OPENSHARP;
}
<DOUBLE_QUOTED_STRING> {DOUBLEQUOTE} {yybegin(YYINITIAL); return CfmlTokenTypes.DOUBLE_QUOTE_CLOSER; }

<SINGLE_QUOTED_STRING> {SINGLEQUOTED_STRING} {yybegin(SINGLEQUOTE_CLOSER); return CfmlTokenTypes.STRING_TEXT;}
<SINGLE_QUOTED_STRING> {SINGLEQUOTED_STRING}/{SHARP_SYMBOL} {return CfmlTokenTypes.STRING_TEXT; }
<SINGLE_QUOTED_STRING> {SHARP_SYMBOL} {
    myCurrentConfiguration.myReturnStack.push(SINGLE_QUOTED_STRING);
    myCurrentConfiguration.mySharpCounter++;
    yybegin(YYINITIAL);
    return CfscriptTokenTypes.OPENSHARP;
}
<SINGLE_QUOTED_STRING> {SINGLEQUOTE} {yybegin(YYINITIAL); return CfmlTokenTypes.SINGLE_QUOTE_CLOSER;}

<DOUBLEQUOTE_CLOSER> {DOUBLEQUOTE} {yybegin(YYINITIAL); return CfmlTokenTypes.DOUBLE_QUOTE_CLOSER; }
<SINGLEQUOTE_CLOSER> {SINGLEQUOTE} {yybegin(YYINITIAL); return CfmlTokenTypes.SINGLE_QUOTE_CLOSER; }

<COMMENTEND> {COMMENTFINISH} { yybegin(myCurrentConfiguration.myReturnStack.pop()); return CfmlTokenTypes.COMMENT; }

<COMMENT> "<"/({COMMENTBEGIN}) { myCurrentConfiguration.myCommentCounter++; return CfmlTokenTypes.COMMENT; }
<COMMENT> "-"/{COMMENTFINISH} { myCurrentConfiguration.myCommentCounter--;
    if (myCurrentConfiguration.myCommentCounter == 0) {
        yybegin(COMMENTEND);
    }
    return CfmlTokenTypes.COMMENT;
}
<COMMENT> [^<-]* {return CfmlTokenTypes.COMMENT;}
<COMMENT> [^] {return CfmlTokenTypes.COMMENT;}

[^]  { return CfscriptTokenTypes.BAD_CHARACTER; }

