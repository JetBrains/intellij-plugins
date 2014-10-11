// We base our lexer directly on the official handlebars.l lexer definition,
// making some modifications to account for Jison/JFlex syntax and functionality differences
//
// Revision ported: https://github.com/wycats/handlebars.js/commit/58a0b4f17d5338793c92cf4d104e9c44cc485c5b#src/handlebars.l

package com.dmarcotte.handlebars.parsing;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.Stack;

// suppress various warnings/inspections for the generated class
@SuppressWarnings ({"FieldCanBeLocal", "UnusedDeclaration", "UnusedAssignment", "AccessStaticViaInstance", "MismatchedReadAndWriteOfArray", "WeakerAccess", "SameParameterValue", "CanBeFinal", "SameReturnValue", "RedundantThrows", "ConstantConditions"})
%%

%class _HbLexer
%implements FlexLexer
%final
%unicode
%function advance
%type IElementType
%eof{ return;
%eof}

%{
    private Stack<Integer> stack = new Stack<Integer>();

    public void yypushState(int newState) {
      stack.push(yystate());
      yybegin(newState);
    }

    public void yypopState() {
      yybegin(stack.pop());
    }
%}

LineTerminator = \r|\n|\r\n
WhiteSpace = {LineTerminator} | [ \t\f]


%state mu
%state emu
%state par
%state comment
%state data

%%

<YYINITIAL> {

  // jflex doesn't support lookaheads with potentially empty prefixes, so we can't directly port the Initial
  // state from handlebars.l, so we accomplish the same thing in a more roundabout way:

  // simulate the lookahead by matching with anything that ends in "{{", and then backtracking away from
  // any trailing "{" characters we've picked up
~"{{" {
          // backtrack over any stache characters at the end of this string
          while (yylength() > 0 && yytext().subSequence(yylength() - 1, yylength()).toString().equals("{")) {
            yypushback(1);
          }

          // inspect the characters leading up to this mustache for escaped characters
          if (yylength() > 1 && yytext().subSequence(yylength() - 2, yylength()).toString().equals("\\\\")) {
            return HbTokenTypes.CONTENT; // double-slash is just more content
          } else if (yylength() > 0 && yytext().toString().substring(yylength() - 1, yylength()).equals("\\")) {
            yypushback(1); // put the escape char back
            yypushState(emu);
          } else {
            yypushState(mu);
          }

          // we stray from the handlebars.js lexer here since we need our WHITE_SPACE more clearly delineated
          //    and we need to avoid creating extra tokens for empty strings (makes the parser and formatter happier)
          if (!yytext().toString().equals("")) {
              if (yytext().toString().trim().length() == 0) {
                  return HbTokenTypes.WHITE_SPACE;
              } else {
                  return HbTokenTypes.CONTENT;
              }
          }
        }

  // Check for anything that is not a string containing "{{"; that's CONTENT
  !([^]*"{{"[^]*)                         { return HbTokenTypes.CONTENT; }
}

<emu> {
    "\\" { return HbTokenTypes.ESCAPE_CHAR; }
    "{{"~"{{" { // grab everything up to the next open stache
          // backtrack over any stache characters or escape characters at the end of this string
          while (yylength() > 0
                  && (yytext().subSequence(yylength() - 1, yylength()).toString().equals("{")
                      || yytext().subSequence(yylength() - 1, yylength()).toString().equals("\\"))) {
            yypushback(1);
          }

          yypopState();
          return HbTokenTypes.CONTENT;
    }
    "{{"!([^]*"{{"[^]*) { // otherwise, if the remaining text just contains the one escaped mustache, then it's all CONTENT
        return HbTokenTypes.CONTENT;
    }
}

<mu> {
  "(" { return HbTokenTypes.OPEN_SEXPR; }
  ")" { return HbTokenTypes.CLOSE_SEXPR; }

  "{{"\~?">" { return HbTokenTypes.OPEN_PARTIAL; }
  "{{"\~?"#" { return HbTokenTypes.OPEN_BLOCK; }
  "{{"\~?"/" { return HbTokenTypes.OPEN_ENDBLOCK; }
  "{{"\~?"^" { return HbTokenTypes.OPEN_INVERSE; }
  // NOTE: a standard Handlebars lexer would check for "{{else" here.  We instead want to lex it as two tokens to highlight the "{{" and the "else" differently.  See where we make an HbTokens.ELSE below.
  "{{"\~?"{" { return HbTokenTypes.OPEN_UNESCAPED; }
  "{{"\~?"&" { return HbTokenTypes.OPEN; }
  "{{!" { yypushback(3); yypopState(); yypushState(comment); }
  "{{"\~? { return HbTokenTypes.OPEN; }
  "=" { return HbTokenTypes.EQUALS; }
  "."/[\~\}\t \n\x0B\f\r] { return HbTokenTypes.ID; }
  ".." { return HbTokenTypes.ID; }
  [\/.] { return HbTokenTypes.SEP; }
  [\t \n\x0B\f\r]* { return HbTokenTypes.WHITE_SPACE; }
  "}"\~?"}}" { yypopState(); return HbTokenTypes.CLOSE_UNESCAPED; }
  \~?"}}" { yypopState(); return HbTokenTypes.CLOSE; }
  \"([^\"\\]|\\.)*\" { return HbTokenTypes.STRING; }
  '([^'\\]|\\.)*' { return HbTokenTypes.STRING; }
  "@" { return HbTokenTypes.DATA_PREFIX; }
  "else"/[}\)\t \n\x0B\f\r] { return HbTokenTypes.ELSE; } // create a custom token for "else" so that we can highlight it independently of the "{{" but still parse it as an inverse operator
  "true"/[}\)\t \n\x0B\f\r] { return HbTokenTypes.BOOLEAN; }
  "false"/[}\)\t \n\x0B\f\r] { return HbTokenTypes.BOOLEAN; }
  \-?[0-9]+(\.[0-9]+)?/[}\)\t \n\x0B\f\r]  { return HbTokenTypes.NUMBER; }
  /*
    ID is the inverse of control characters.
    Control characters ranges:
      [\\t \n\x0B\f\r]          Whitespace
      [!"#%-,\./]   !, ", #, %, &, ', (, ), *, +, ,, ., /,  Exceptions in range: $, -
      [;->@]        ;, <, =, >, @,                          Exceptions in range: :, ?
      [\[-\^`]      [, \, ], ^, `,                          Exceptions in range: _
      [\{-~]        {, |, }, ~
    */
  [^\t \n\x0B\f\r!\"#%-,\.\/;->@\[-\^`\{-~]+/[\~=}\)\t \n\x0B\f\r\/.]   { return HbTokenTypes.ID; }
  // TODO handlesbars.l extracts the id from within the square brackets.  Fix it to match handlebars.l?
  "["[^\]]*"]" { return HbTokenTypes.ID; }
}

<comment> {
  "{{!--"~"--}}" { yypopState(); return HbTokenTypes.COMMENT; }
  "{{!}}" { yypopState(); return HbTokenTypes.COMMENT; }
  "{{!"[^"--"}]~"}}" {
      // backtrack over any extra stache characters at the end of this string
      while (yylength() > 2 && yytext().subSequence(yylength() - 3, yylength()).toString().equals("}}}")) {
        yypushback(1);
      }
      yypopState();
      return HbTokenTypes.COMMENT;
  }
  // lex unclosed comments so that we can give better errors
  "{{!--"!([^]*"--}}"[^]*) { yypopState(); return HbTokenTypes.UNCLOSED_COMMENT; }
  "{{!"!([^]*"}}"[^]*) { yypopState(); return HbTokenTypes.UNCLOSED_COMMENT; }
}

{WhiteSpace}+ { return HbTokenTypes.WHITE_SPACE; }
. { return HbTokenTypes.INVALID; }