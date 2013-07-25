package com.intellij.coldFusion.model.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.coldFusion.model.psi.CfmlElementType;
import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.coldFusion.model.CfmlUtil;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.Stack;

%%

%{
  CfmlLexerConfiguration myCurrentConfiguration = new CfmlLexerConfiguration();
  private Project myProject;

  public _CfmlLexer(Project project) {
    this((java.io.Reader)null);
    myProject =  project;
  }

  public class CfmlLexerConfiguration {
      public int myArePoundsEvaluated = 0;
      public int myCommentCounter = 0;
      public int mySharpCounter = 0;
      public boolean myIfReturnExpression = false;
      public Stack<Integer> myReturnStack = new Stack<Integer>();
      // to give to other lexer
      public IElementType myBlockType = CfmlElementTypes.TEMPLATE_TEXT;
      public boolean myStartExpression = true;
      public String myCurrentTag = "cfelse";

      public CfmlLexerConfiguration() {}

      public void reset() {
          myCommentCounter = 0;
          mySharpCounter = 0;
          myIfReturnExpression = false;
          myReturnStack.clear();
          myBlockType = CfmlElementTypes.TEMPLATE_TEXT;
          myStartExpression = true;
          myCurrentTag = "cfelse";
      }
  }

  private void releaseExpressionState() {
    myCurrentConfiguration.mySharpCounter = 0;
    myCurrentConfiguration.myIfReturnExpression = false;
    myCurrentConfiguration.myReturnStack.clear();
  }

  private IElementType startComment(int stateToReturnTo) {
    myCurrentConfiguration.myReturnStack.push(stateToReturnTo);
    myCurrentConfiguration.myCommentCounter++;
    yybegin(COMMENT);
    return CfmlTokenTypes.COMMENT;
  }

  private IElementType startTag() {
    releaseExpressionState();
    yybegin(TAGOPEN); return CfmlTokenTypes.OPENER;
  }

  private IElementType startCloseTag() {
    // myCurrentConfiguration.myArePoundsEvaluated --;
    yybegin(TAGCLOSE); return CfmlTokenTypes.LSLASH_ANGLEBRACKET;
  }

  private void processCloseTag(String tagName) {
    if ("cfquery".equalsIgnoreCase(tagName) ||
        "cfqueryparam".equalsIgnoreCase(tagName) ||
        "cfoutput".equalsIgnoreCase(tagName) ||
        "cfmail".equalsIgnoreCase(tagName)) {
        myCurrentConfiguration.myArePoundsEvaluated --;
    }
  }

  private IElementType closeStartedTag() {
        myCurrentConfiguration.myStartExpression = true;
        if (myCurrentConfiguration.myCurrentTag.equalsIgnoreCase("cfscript")) {
            myCurrentConfiguration.myStartExpression = false;
            myCurrentConfiguration.myBlockType = CfmlTokenTypes.SCRIPT_EXPRESSION;
            yybegin(YYINITIAL);
        } else if (myCurrentConfiguration.myCurrentTag.equalsIgnoreCase("cfquery") ||
                   myCurrentConfiguration.myCurrentTag.equalsIgnoreCase("cfqueryparam")) {
            myCurrentConfiguration.myBlockType = CfmlElementTypes.SQL;
            myCurrentConfiguration.myArePoundsEvaluated++;
            yybegin(YYINITIAL);
        } else if (myCurrentConfiguration.myCurrentTag.equalsIgnoreCase("cfoutput") ||
                   myCurrentConfiguration.myCurrentTag.equalsIgnoreCase("cfmail")) {
            myCurrentConfiguration.myArePoundsEvaluated++;
            // yybegin(TEXT);
            yybegin(YYINITIAL);
        } else {
            yybegin(YYINITIAL);
        }
        if (CfmlUtil.isSingleCfmlTag(myCurrentConfiguration.myCurrentTag, myProject))
            return CfmlTokenTypes.CLOSER;
        return CfmlTokenTypes.R_ANGLEBRACKET;
  }

  private IElementType startExpression(int stateToReturn) {
        myCurrentConfiguration.mySharpCounter++;
        myCurrentConfiguration.myReturnStack.push(stateToReturn);
        yybegin(SCRIPT_EXPRESSION);
        if (myCurrentConfiguration.mySharpCounter == 1) {
            return myCurrentConfiguration.myStartExpression ? CfmlTokenTypes.START_EXPRESSION : CfmlTokenTypes.SCRIPT_EXPRESSION;
        }
        return CfmlTokenTypes.SCRIPT_EXPRESSION;
  }

  private IElementType closeTag() {
    yybegin(YYINITIAL); return CfmlTokenTypes.CLOSER;
  }
%}

%class _CfmlLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}
%caseless
%ignorecase

WHITE_SPACE_CHAR=[\ \n\r\t\f]*
WHITE_PREFIX=[\n]{WHITE_SPACE_CHAR}*

COMMENTSTART = "<!---"
COMMENTBEGIN = "!---"
COMMENTFINISH = "-->"

ATTRIBUTEVALUE = [\ \n\r\t\f]*"="

CF_TAG_NAME = (cf|Cf|cF|CF)_?[:jletterdigit:]*
CF_STANDART_TAG = {CF_TAG_NAME}//(cf|Cf|cF|CF)[:jletter:]*
CF_CUSTOM_TAG = (cf|Cf|cF|CF)"_"[:jletter:]*

IDENTIFIER=[:jletter:] [:jletterdigit:]*

CFTAG_NAME_WITH_PREFIX = {CF_TAG_NAME}":"{IDENTIFIER}

SHARP_SYMBOL = #

DOUBLEQUOTE = \"
SINGLEQUOTE = \'

DOUBLEQUOTED_STRING = ([^#\"] | \"\" | "##")+
SINGLEQUOTED_STRING = ([^#\'] | \'\' | "##")+

TAG_START = "<"
TAG_END = "</"

CFML_TAG_START={TAG_START}{WHITE_SPACE_CHAR}{CF_TAG_NAME}

PERIODICAL_NAME = {IDENTIFIER}("."{IDENTIFIER})* 
IMPORT = "import"({DOUBLEQUOTED_STRING} | {SINGLEQUOTED_STRING} | {PERIODICAL_NAME})
SCRIPT_COMPONENT_DEF = ((("/*")~("*/"))? {WHITE_SPACE_CHAR} {IMPORT}? {WHITE_SPACE_CHAR})* ("component" | "interface"){NOTCFMLSTART_NOTSHARP}

NOTTAGORCOMMENTBEGIN = ("<"[^c/C!] | "<c"[^fF] | "<C"[^fF] | "</"[^Cc] | "</c"[^Ff] | "</C"[^fF] |
             "<!"[^-] | "<!-"[^-] | "<!--"[^-])
NOTCFMLSTART = ([^<] | {NOTTAGORCOMMENTBEGIN})+
NOTCFMLSTART_NOTSHARP = ([^#<@] | {NOTTAGORCOMMENTBEGIN})([^#<] | {NOTTAGORCOMMENTBEGIN})*
NOTCFMLSTART_NOTSHARP_NOTQUOTE = ([^#<\'\">/] | "/"[^>] | {NOTTAGORCOMMENTBEGIN})+
NOTCFMLSTART_NOTSHARP_NOTQUOTE_NOTWHITESPACE = ([^#<\'\">/\ \n\r\t\f] | "/"[^>])+
VARIABLE_TYPE_DECL = {COMMENTSTART}{WHITE_SPACE_CHAR}"@cfmlvariable"(~{COMMENTFINISH}){COMMENTFINISH}?

WHAT_EVER=[^]*

%state CLOSER, TAGCLOSE, TAGOPEN, TAGATTR, ASSIGN
%state COMMENT, COMMENTEND
%state DOUBLE_QUOTED_STRING, DOUBLEQUOTE, SINGLE_QUOTED_STRING, SINGLEQUOTE, DOUBLEQUOTE_CLOSER, SINGLEQUOTE_CLOSER, ATTRIBUTE_VALUE
%state TAGINNERBLOCK
%state SCRIPT_EXPRESSION
%state X, Y
%state TEXT
%state EAT_TEST_AS_SCRIPT

%ignorecase
%char

%%

<YYINITIAL> {SCRIPT_COMPONENT_DEF} {if (yychar == 0) { yybegin(EAT_TEST_AS_SCRIPT); return CfmlElementTypes.CF_SCRIPT; }
                                                                 else { return myCurrentConfiguration.myBlockType; }}
<YYINITIAL> {VARIABLE_TYPE_DECL} { return CfmlTokenTypes.VAR_ANNOTATION; }
<YYINITIAL> {TAG_START}/{CF_TAG_NAME}  {
    myCurrentConfiguration.myBlockType = CfmlElementTypes.TEMPLATE_TEXT;
    return startTag();
}
<YYINITIAL> {TAG_END}/{CF_TAG_NAME}  { myCurrentConfiguration.myBlockType = CfmlElementTypes.TEMPLATE_TEXT; return startCloseTag(); }
<YYINITIAL> {COMMENTSTART} {return startComment(YYINITIAL);}
<YYINITIAL> {SHARP_SYMBOL} {
    if (myCurrentConfiguration.myArePoundsEvaluated > 0) {
        return startExpression(YYINITIAL);
    } else {
        return myCurrentConfiguration.myBlockType;
    }
}
<YYINITIAL> {WHITE_SPACE_CHAR} {   if(myCurrentConfiguration.myBlockType == CfmlTokenTypes.SCRIPT_EXPRESSION){ return CfmlTokenTypes.SCRIPT_EXPRESSION;} return CfmlTokenTypes.WHITE_SPACE; }
//<YYINITIAL> {WHITE_PREFIX}/{CFML_TAG_START} { return CfmlTokenTypes.WHITE_SPACE; }
<YYINITIAL> {NOTCFMLSTART_NOTSHARP} { return myCurrentConfiguration.myBlockType; }

/*<TAGOPEN> {CF_CUSTOM_TAG} {
    myCurrentConfiguration.myCurrentTag = yytext().toString().toLowerCase();
    myCurrentConfiguration.myStartExpression = true;
    yybegin(TAGATTR);
    return CfmlTokenTypes.CF_CUSTOM_TAG_NAME;
}*/
<TAGOPEN> {CFTAG_NAME_WITH_PREFIX}  {
    myCurrentConfiguration.myCurrentTag = yytext().toString().toLowerCase();
    if (!CfmlUtil.hasAnyAttributes(myCurrentConfiguration.myCurrentTag, myProject)) {
        myCurrentConfiguration.myStartExpression = false;
        yybegin(SCRIPT_EXPRESSION);
    } else {
        myCurrentConfiguration.myStartExpression = true;
        yybegin(TAGATTR);
    }
    return CfmlTokenTypes.CF_TAG_NAME;
}

<TAGOPEN> {CF_STANDART_TAG}  {
    myCurrentConfiguration.myCurrentTag = yytext().toString().toLowerCase();
    if (!CfmlUtil.hasAnyAttributes(myCurrentConfiguration.myCurrentTag, myProject)) {
        myCurrentConfiguration.myStartExpression = false;
        yybegin(SCRIPT_EXPRESSION);
    } else {
        myCurrentConfiguration.myStartExpression = true;
        yybegin(TAGATTR);
    }
    return CfmlTokenTypes.CF_TAG_NAME;
}
<TAGOPEN> {WHITE_SPACE_CHAR} { return CfmlTokenTypes.WHITE_SPACE; }
<TAGOPEN> [^]  { yybegin(YYINITIAL); return CfmlTokenTypes.BAD_CHARACTER; }

/*<TAGCLOSE> {CF_CUSTOM_TAG} { yybegin(CLOSER); return CfmlTokenTypes.CF_CUSTOM_TAG_NAME; }*/
<TAGCLOSE> {CFTAG_NAME_WITH_PREFIX} { yybegin(CLOSER); processCloseTag(yytext().toString()); return CfmlTokenTypes.CF_TAG_NAME; }
<TAGCLOSE> {CF_STANDART_TAG} { yybegin(CLOSER); processCloseTag(yytext().toString()); return CfmlTokenTypes.CF_TAG_NAME; }
<TAGCLOSE> {WHITE_SPACE_CHAR} { return CfmlTokenTypes.WHITE_SPACE; }
<TAGCLOSE> [^]  { yybegin(YYINITIAL); return CfmlTokenTypes.BAD_CHARACTER; }

<CLOSER> ">" { return closeTag(); }
<CLOSER> {WHITE_SPACE_CHAR} { return CfmlTokenTypes.WHITE_SPACE; }
<CLOSER> [^>] { yybegin(YYINITIAL); return CfmlTokenTypes.BAD_CHARACTER; }

<TAGATTR> {WHITE_SPACE_CHAR}  { return CfmlTokenTypes.WHITE_SPACE; }
<TAGATTR> {IDENTIFIER} {
    yybegin(ASSIGN);
    return CfmlTokenTypes.ATTRIBUTE;
}
<TAGATTR> {IDENTIFIER}":"{IDENTIFIER} {
    yybegin(ASSIGN);
    return CfmlTokenTypes.ATTRIBUTE;
}
<TAGATTR> {COMMENTSTART} {return startComment(TAGATTR);}
<TAGATTR> ">" { return closeStartedTag(); }
<TAGATTR> "/>" { return closeTag(); }
<TAGATTR> {TAG_START}/{CF_TAG_NAME} { return startTag(); }
<TAGATTR> {TAG_END}/{CF_TAG_NAME}  { return startCloseTag(); }
<TAGATTR> {WHITE_SPACE_CHAR}  { return CfmlTokenTypes.WHITE_SPACE; }
<TAGATTR> [^]  { yybegin(YYINITIAL); return CfmlTokenTypes.BAD_CHARACTER; }

<ASSIGN> {WHITE_SPACE_CHAR}  { return CfmlTokenTypes.WHITE_SPACE; }
<ASSIGN> "=" { yybegin(ATTRIBUTE_VALUE); return CfmlTokenTypes.ASSIGN; }
<ASSIGN> {COMMENTSTART} {return startComment(TAGATTR);}
<ASSIGN> ">" { return closeStartedTag(); }
<ASSIGN> "/>" { return closeTag(); }
<ASSIGN> {TAG_START}/{CF_TAG_NAME}  { yybegin(TAGOPEN); return CfmlTokenTypes.OPENER; }
<ASSIGN> {TAG_END}/{CF_TAG_NAME}  { yybegin(TAGCLOSE); return CfmlTokenTypes.LSLASH_ANGLEBRACKET; }
<ASSIGN> [^] { yybegin(ATTRIBUTE_VALUE); return CfmlTokenTypes.BAD_CHARACTER; }

// Comments
<COMMENTEND> {COMMENTFINISH} { yybegin(myCurrentConfiguration.myReturnStack.pop()); return CfmlTokenTypes.COMMENT; }
<COMMENT> "<"/({COMMENTBEGIN}) { myCurrentConfiguration.myCommentCounter++; return CfmlTokenTypes.COMMENT; }
<COMMENT> "-"/({COMMENTFINISH}) { myCurrentConfiguration.myCommentCounter--;
    if (myCurrentConfiguration.myCommentCounter == 0) {
        yybegin(COMMENTEND);
    }
    return CfmlTokenTypes.COMMENT;
}
<COMMENT> [^<-]* {return CfmlTokenTypes.COMMENT;}
<COMMENT> [^] {return CfmlTokenTypes.COMMENT;}

// text inside cfoutput
<TEXT> {NOTCFMLSTART_NOTSHARP} {return CfmlElementTypes.TEMPLATE_TEXT; }
<TEXT> {SHARP_SYMBOL} {
    return startExpression(TEXT);
}
<TEXT> {COMMENTSTART} {return startComment(TEXT);}
<TEXT> {TAG_START}/{CF_TAG_NAME}  { return startTag(); }
<TEXT> {TAG_END}/{CF_TAG_NAME}  { return startCloseTag(); }

/* strings */
<ATTRIBUTE_VALUE> {WHITE_SPACE_CHAR} {return CfmlTokenTypes.WHITE_SPACE; }
<ATTRIBUTE_VALUE> {SHARP_SYMBOL} {
    return startExpression(TAGATTR);
}
<ATTRIBUTE_VALUE> {DOUBLEQUOTE} {
    myCurrentConfiguration.myReturnStack.push(TAGATTR);
    yybegin(DOUBLE_QUOTED_STRING);
    return CfmlTokenTypes.DOUBLE_QUOTE;
}
<ATTRIBUTE_VALUE> {SINGLEQUOTE} {
    myCurrentConfiguration.myReturnStack.push(TAGATTR);
    yybegin(SINGLE_QUOTED_STRING);
    return CfmlTokenTypes.SINGLE_QUOTE;
}
<ATTRIBUTE_VALUE> {NOTCFMLSTART_NOTSHARP_NOTQUOTE_NOTWHITESPACE} {
    yybegin(TAGATTR);
    return CfmlElementTypes.CF_SCRIPT;
}

<DOUBLE_QUOTED_STRING> {DOUBLEQUOTED_STRING} {
    yybegin(DOUBLEQUOTE_CLOSER);
    if (myCurrentConfiguration.myIfReturnExpression) {
        return CfmlTokenTypes.SCRIPT_EXPRESSION;
    }
    return CfmlTokenTypes.STRING_TEXT;
}
<DOUBLE_QUOTED_STRING> {DOUBLEQUOTED_STRING}/{SHARP_SYMBOL} {
    if (myCurrentConfiguration.myIfReturnExpression) {
        return CfmlTokenTypes.SCRIPT_EXPRESSION;
    }
    return CfmlTokenTypes.STRING_TEXT;
}
<DOUBLE_QUOTED_STRING> {SHARP_SYMBOL} {
    return startExpression(DOUBLE_QUOTED_STRING);
}
<DOUBLE_QUOTED_STRING> {DOUBLEQUOTE} {
    yybegin(myCurrentConfiguration.myReturnStack.pop());
    if (myCurrentConfiguration.myIfReturnExpression) {
        return CfmlTokenTypes.SCRIPT_EXPRESSION;
    }
    return CfmlTokenTypes.DOUBLE_QUOTE_CLOSER;
}

<SINGLE_QUOTED_STRING> {SINGLEQUOTED_STRING} {
    yybegin(SINGLEQUOTE_CLOSER);
    if (myCurrentConfiguration.myIfReturnExpression) {
        return CfmlTokenTypes.SCRIPT_EXPRESSION;
    }
    return CfmlTokenTypes.STRING_TEXT;
}
<SINGLE_QUOTED_STRING> {SINGLEQUOTED_STRING}/{SHARP_SYMBOL} {
    if (myCurrentConfiguration.myIfReturnExpression) {
        return CfmlTokenTypes.SCRIPT_EXPRESSION;
    }
    return CfmlTokenTypes.STRING_TEXT;
}
<SINGLE_QUOTED_STRING> {SHARP_SYMBOL} {
    return startExpression(SINGLE_QUOTED_STRING);
}
<SINGLE_QUOTED_STRING> {SINGLEQUOTE} {
    yybegin(myCurrentConfiguration.myReturnStack.pop());
    if (myCurrentConfiguration.myIfReturnExpression) {
        return CfmlTokenTypes.SCRIPT_EXPRESSION;
    }
    return CfmlTokenTypes.SINGLE_QUOTE_CLOSER;
}

<DOUBLEQUOTE_CLOSER> {DOUBLEQUOTE} {
    yybegin(myCurrentConfiguration.myReturnStack.pop());
    if (myCurrentConfiguration.myIfReturnExpression) {
        return CfmlTokenTypes.SCRIPT_EXPRESSION;
    }
    return CfmlTokenTypes.DOUBLE_QUOTE_CLOSER;
}
<SINGLEQUOTE_CLOSER> {SINGLEQUOTE} {
    yybegin(myCurrentConfiguration.myReturnStack.pop());
    if (myCurrentConfiguration.myIfReturnExpression) {
        return CfmlTokenTypes.SCRIPT_EXPRESSION;
    }
    return CfmlTokenTypes.SINGLE_QUOTE_CLOSER;
}

<SCRIPT_EXPRESSION> {NOTCFMLSTART_NOTSHARP_NOTQUOTE} {
    myCurrentConfiguration.myIfReturnExpression = true;
    return CfmlTokenTypes.SCRIPT_EXPRESSION;
}
<SCRIPT_EXPRESSION> "/" {
    myCurrentConfiguration.myIfReturnExpression = true;
    return CfmlTokenTypes.SCRIPT_EXPRESSION;
}
<SCRIPT_EXPRESSION> {SHARP_SYMBOL} {
    myCurrentConfiguration.myIfReturnExpression = true;
    if (!myCurrentConfiguration.myStartExpression && myCurrentConfiguration.mySharpCounter == 0) {
        return startExpression(SCRIPT_EXPRESSION);
    }
    myCurrentConfiguration.mySharpCounter--;
    yybegin(myCurrentConfiguration.myReturnStack.pop());
    if (myCurrentConfiguration.mySharpCounter == 0 && myCurrentConfiguration.myStartExpression) {
        myCurrentConfiguration.myIfReturnExpression = false;
        return CfmlTokenTypes.END_EXPRESSION;
    }
    return CfmlTokenTypes.SCRIPT_EXPRESSION;
}
<SCRIPT_EXPRESSION> {DOUBLEQUOTE} {
    myCurrentConfiguration.myIfReturnExpression = true;
    myCurrentConfiguration.myReturnStack.push(SCRIPT_EXPRESSION);
    yybegin(DOUBLE_QUOTED_STRING);
    return CfmlTokenTypes.SCRIPT_EXPRESSION;
}

<SCRIPT_EXPRESSION> {SINGLEQUOTE} {
    myCurrentConfiguration.myIfReturnExpression = true;
    myCurrentConfiguration.myReturnStack.push(SCRIPT_EXPRESSION);
    yybegin(SINGLE_QUOTED_STRING);
    return CfmlTokenTypes.SCRIPT_EXPRESSION;
}

<SCRIPT_EXPRESSION> {COMMENTSTART} {return startComment(SCRIPT_EXPRESSION);}
<SCRIPT_EXPRESSION> ">" { releaseExpressionState(); return closeStartedTag(); }
<SCRIPT_EXPRESSION> "/>" { releaseExpressionState(); return closeTag(); }
<SCRIPT_EXPRESSION> {TAG_START}/{CF_TAG_NAME}  { releaseExpressionState(); yybegin(TAGOPEN); return CfmlTokenTypes.OPENER; }
<SCRIPT_EXPRESSION> {TAG_END}/{CF_TAG_NAME}  { releaseExpressionState(); yybegin(TAGCLOSE); return CfmlTokenTypes.LSLASH_ANGLEBRACKET; }
<SCRIPT_EXPRESSION> [^]  { releaseExpressionState(); yybegin(YYINITIAL); return CfmlTokenTypes.BAD_CHARACTER; }

<EAT_TEST_AS_SCRIPT> {WHAT_EVER} {return CfmlElementTypes.CF_SCRIPT; }

[^] { yybegin(YYINITIAL); return CfmlTokenTypes.BAD_CHARACTER; }
