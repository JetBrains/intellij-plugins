 /* It's an automatically generated code. Do not modify it. */
package org.intellij.plugins.postcss.lexer;
import com.intellij.psi.css.impl.*;
import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.openapi.util.text.StringUtil;
import org.intellij.plugins.postcss.lexer.PostCssTokenTypes;

%%

%{
  public _PostCssLexer() {
    this((java.io.Reader)null);
  }
%}

%class _PostCssLexer
%public
%implements FlexLexer
%unicode
%function advance
%type IElementType

%state CSS_URI
%state CSS_FUNCTION
%state CSS_FUNCTION_MINUS_N
%state CSS_COMMENT

DIGIT=[0-9]
WS=[\ \n\r\t\f]
WSP={WS}+

CSS_NONASCII=[^\000-\177]
CSS_H=[0-9a-fA-F]
HEX_DIGIT_1_TO_6={CSS_H}{1,6}
CSS_UNICODE=\\{HEX_DIGIT_1_TO_6}+
CSS_UNICODE_RANGE=U\+{HEX_DIGIT_1_TO_6}"-"{HEX_DIGIT_1_TO_6}
CSS_UNICODE_RANGE_MASK=U\+({CSS_H}|\?){1,6}
CSS_ESCAPE={CSS_UNICODE}|\\[\ -~]
CSS_NMSTART=([a-zA-Z_]|{CSS_NONASCII}|{CSS_ESCAPE})
CSS_NMCHAR=({CSS_NMSTART}|[0-9\-_])
CSS_NUMBER={DIGIT}+|{DIGIT}*\.{DIGIT}+
CSS_NL=\n|\r\n|\r|\f
CSS_STRING_CHAR=[^\n\r\f\"\\]|{CSS_ESCAPE}|\\{CSS_NL}
CSS_STRING_CHAR2=[^\n\r\f\'\\]|{CSS_ESCAPE}|\\{CSS_NL}
CSS_STRING1=\"({CSS_STRING_CHAR}|\')*(\")?
CSS_STRING2=\'({CSS_STRING_CHAR2}|\")*(\')?

CSS_IDENT=([\-]?{CSS_NMSTART}{CSS_NMCHAR}*)|("--"{CSS_NMCHAR}*)
CSS_NAME={CSS_NMCHAR}+
CSS_STRING={CSS_STRING1}|{CSS_STRING2}
CSS_W={WS}*
CSS_URL=([!#\$%&\*-\~]|{CSS_NONASCII}|{CSS_ESCAPE})*
EL_EMBEDDMENT= ("#{" | "${" ) [^\}]* "}"
CSS_HACKS="\\0/"|"\\9"|"\\0"|"!"{I}{E}

POST_CSS_SIMPLE_VARIABLE_TOKEN="$"{CSS_IDENT} | "$" "(" {CSS_IDENT} ")"?

A=[Aa]
B=[Bb]
C=[Cc]
D=[Dd]
E=[Ee]
F=[Ff]
G=[Gg]
H=[Hh]
I=[Ii]
J=[Jj]
K=[Kk]
L=[Ll]
M=[Mm]
N=[Nn]
O=[Oo]
P=[Pp]
Q=[Qq]
R=[Rr]
S=[Ss]
T=[Tt]
U=[Uu]
V=[Vv]
W=[Ww]
X=[Xx]
Y=[Yy]
Z=[Zz]

MARGIN_SYM = @{T}{O}{P}-{L}{E}{F}{T}-{C}{O}{R}{N}{E}{R} | @{T}{O}{P}-{L}{E}{F}{T} | @{T}{O}{P}-{C}{E}{N}{T}{E}{R} | 
             @{T}{O}{P}-{R}{I}{G}{H}{T} | @{T}{O}{P}-{R}{I}{G}{H}{T}-{C}{O}{R}{N}{E}{R} |
             @{B}{O}{T}{T}{O}{M}-{L}{E}{F}{T}-{C}{O}{R}{N}{E}{R} | @{B}{O}{T}{T}{O}{M}-{L}{E}{F}{T} | 
             @{B}{O}{T}{T}{O}{M}-{C}{E}{N}{T}{E}{R} | @{B}{O}{T}{T}{O}{M}-{R}{I}{G}{H}{T} | @{B}{O}{T}{T}{O}{M}-{R}{I}{G}{H}{T}-{C}{O}{R}{N}{E}{R} |
             @{L}{E}{F}{T}-{T}{O}{P} | @{L}{E}{F}{T}-{M}{I}{D}{D}{L}{E} | @{L}{E}{F}{T}-{B}{O}{T}{T}{O}{M} |
             @{R}{I}{G}{H}{T}-{T}{O}{P} | @{R}{I}{G}{H}{T}-{M}{I}{D}{D}{L}{E} | @{R}{I}{G}{H}{T}-{B}{O}{T}{T}{O}{M}

URL_PREFIX_DOMAIN={U}{R}{L}|{U}{R}{L}-{P}{R}{E}{F}{I}{X}|{D}{O}{M}{A}{I}{N}
%%

<YYINITIAL> {URL_PREFIX_DOMAIN}"(" { yybegin(CSS_URI); yypushback(yylength()); }
<CSS_URI> {URL_PREFIX_DOMAIN} { return CssElementTypes.CSS_URI_START; }
<CSS_URI> "(" { return CssElementTypes.CSS_LPAREN; }
<CSS_URI> ")" { yybegin(YYINITIAL); return CssElementTypes.CSS_RPAREN; }
<CSS_URI> {CSS_URL} { return CssElementTypes.CSS_URL; }
<CSS_URI> {WSP} { return CssElementTypes.CSS_WHITE_SPACE; }
<CSS_URI> [^] { return CssElementTypes.CSS_BAD_CHARACTER; }
<YYINITIAL> "expression(" { yypushback(1); return CssElementTypes.CSS_EXPRESSION; }
<YYINITIAL> "-n" { yybegin(CSS_FUNCTION_MINUS_N); yypushback(yylength()); }
<YYINITIAL> {CSS_HACKS} { return CssElementTypes.CSS_HACK; }
<YYINITIAL> "{" { return CssElementTypes.CSS_LBRACE; }
<YYINITIAL> "}" { return CssElementTypes.CSS_RBRACE; }
<YYINITIAL> ":" { return CssElementTypes.CSS_COLON; }
<YYINITIAL> ";" { return CssElementTypes.CSS_SEMICOLON; }
<YYINITIAL> {CSS_NUMBER} { return CssElementTypes.CSS_NUMBER; }
<YYINITIAL> "," { return CssElementTypes.CSS_COMMA; }
<YYINITIAL> "%" { return CssElementTypes.CSS_PERCENT; }
<YYINITIAL> "." { return CssElementTypes.CSS_PERIOD; }
<YYINITIAL> ">" { return CssElementTypes.CSS_GT; }
<YYINITIAL> ">>>" { return CssElementTypes.CSS_GT_GT_GT; }
<YYINITIAL> "/"{CSS_IDENT}"/" { return CssElementTypes.CSS_SLASHED_COMBINATOR; }
<YYINITIAL> "+" { return CssElementTypes.CSS_PLUS; }
<YYINITIAL> "*" { return CssElementTypes.CSS_ASTERISK; }
<YYINITIAL> "#"{CSS_NAME}{CSS_HACKS} { if(StringUtil.endsWith(yytext(), "\\0") || StringUtil.endsWith(yytext(), "\\9")) yypushback(2); else yypushback(3); return CssElementTypes.CSS_HASH; }
<YYINITIAL> "#"{CSS_NAME} { return CssElementTypes.CSS_HASH; }
<YYINITIAL> "/*" { yybegin(CSS_COMMENT); return CssElementTypes.CSS_COMMENT; }
<CSS_COMMENT> "*/" { yybegin(YYINITIAL); return CssElementTypes.CSS_COMMENT; }
<CSS_COMMENT> [^] { return CssElementTypes.CSS_COMMENT; }
<YYINITIAL> "-" { return CssElementTypes.CSS_MINUS; }
<YYINITIAL,CSS_URI> {CSS_STRING} { return CssElementTypes.CSS_STRING_TOKEN; }
<YYINITIAL> "[" { return CssElementTypes.CSS_LBRACKET; }
<YYINITIAL> "]" { return CssElementTypes.CSS_RBRACKET; }
<YYINITIAL> "~" { return CssElementTypes.CSS_TILDA; }
<YYINITIAL> "=" { return CssElementTypes.CSS_EQ; }
<YYINITIAL> "!"{CSS_W}{I}{M}{P}{O}{R}{T}{A}{N}{T} { return CssElementTypes.CSS_IMPORTANT; }
<YYINITIAL> @{M}{E}{D}{I}{A} { return CssElementTypes.CSS_MEDIA_SYM; }
<YYINITIAL> @{P}{A}{G}{E} { return CssElementTypes.CSS_PAGE_SYM; }
<YYINITIAL> {WSP} { return CssElementTypes.CSS_WHITE_SPACE; }
<YYINITIAL> "<!--" { return CssElementTypes.CSS_CDO; }
<YYINITIAL> "-->" { return CssElementTypes.CSS_CDC; }
<YYINITIAL> @{I}{M}{P}{O}{R}{T} { return CssElementTypes.CSS_IMPORT_SYM; }
<YYINITIAL> @{S}{U}{P}{P}{O}{R}{T}{S} { return CssElementTypes.CSS_SUPPORTS_SYM; }
<YYINITIAL> @{V}{I}{E}{W}{P}{O}{R}{T} { return CssElementTypes.CSS_VIEWPORT_SYM; }
<YYINITIAL> @-{W}{E}{B}{K}{I}{T}-{V}{I}{E}{W}{P}{O}{R}{T} { return CssElementTypes.CSS_VIEWPORT_SYM; }
<YYINITIAL> @-{M}{O}{Z}-{V}{I}{E}{W}{P}{O}{R}{T} { return CssElementTypes.CSS_VIEWPORT_SYM; }
<YYINITIAL> @-{M}{S}-{V}{I}{E}{W}{P}{O}{R}{T} { return CssElementTypes.CSS_VIEWPORT_SYM; }
<YYINITIAL> @-{O}-{V}{I}{E}{W}{P}{O}{R}{T} { return CssElementTypes.CSS_VIEWPORT_SYM; }
<YYINITIAL> @{R}{E}{G}{I}{O}{N} { return CssElementTypes.CSS_REGION_SYM; }
<YYINITIAL> @{R}{E}{G}{I}{O}{N}-{S}{T}{Y}{L}{E} { return CssElementTypes.CSS_REGION_SYM; }
<YYINITIAL> @-{W}{E}{B}{K}{I}{T}-{R}{E}{G}{I}{O}{N} { return CssElementTypes.CSS_REGION_SYM; }
<YYINITIAL> @-{W}{E}{B}{K}{I}{T}-{R}{E}{G}{I}{O}{N}-{S}{T}{Y}{L}{E} { return CssElementTypes.CSS_REGION_SYM; }
<YYINITIAL> @{D}{O}{C}{U}{M}{E}{N}{T} { return CssElementTypes.CSS_DOCUMENT_SYM; }
<YYINITIAL> @-{M}{O}{Z}-{D}{O}{C}{U}{M}{E}{N}{T} { return CssElementTypes.CSS_DOCUMENT_SYM; }
<YYINITIAL> @{N}{A}{M}{E}{S}{P}{A}{C}{E} { return CssElementTypes.CSS_NAMESPACE_SYM; }
<YYINITIAL> @{S}{C}{O}{P}{E} { return CssElementTypes.CSS_SCOPE_SYM; }
<YYINITIAL> @{C}{O}{U}{N}{T}{E}{R}-{S}{T}{Y}{L}{E} { return CssElementTypes.CSS_COUNTER_STYLE_SYM; }
<YYINITIAL> @{V}{A}{L}{U}{E} { return CssElementTypes.CSS_VALUE_SYM; }
// PostCSS specific
<YYINITIAL> @{N}{E}{S}{T} { return PostCssTokenTypes.POST_CSS_NEST_SYM; }
<YYINITIAL> @{C}{U}{S}{T}{O}{M}-{S}{E}{L}{E}{C}{T}{O}{R} { return PostCssTokenTypes.POST_CSS_CUSTOM_SELECTOR_SYM; }
<YYINITIAL> @{C}{U}{S}{T}{O}{M}-{M}{E}{D}{I}{A} { return PostCssTokenTypes.POST_CSS_CUSTOM_MEDIA_SYM; }

<YYINITIAL> "~=" { return CssElementTypes.CSS_INCLUDES; }
<YYINITIAL> "|=" { return CssElementTypes.CSS_DASHMATCH; }
<YYINITIAL> "|" { return CssElementTypes.CSS_PIPE; }
<YYINITIAL> "^=" { return CssElementTypes.CSS_BEGINS_WITH; }
<YYINITIAL> "$=" { return CssElementTypes.CSS_ENDS_WITH; }
<YYINITIAL> "*=" { return CssElementTypes.CSS_CONTAINS; }
<YYINITIAL> "!=" { return CssElementTypes.CSS_JQUERY_NOT_EQUALS; }
<YYINITIAL> "/" { return CssElementTypes.CSS_SLASH; }
<YYINITIAL> "(" { return CssElementTypes.CSS_LPAREN; }
<YYINITIAL> ")" { return CssElementTypes.CSS_RPAREN; }
<YYINITIAL> "^^" { return CssElementTypes.CSS_CAT; }
<YYINITIAL> "^" { return CssElementTypes.CSS_HAT; }
<CSS_FUNCTION_MINUS_N> "-" { return CssElementTypes.CSS_MINUS; }
<CSS_FUNCTION_MINUS_N> "n" { yybegin(YYINITIAL); return CssElementTypes.CSS_IDENT; }
// PostCSS specific
<YYINITIAL> "//".* { return PostCssTokenTypes.POST_CSS_COMMENT; }
<YYINITIAL> "&" { return PostCssTokenTypes.AMPERSAND; }
<YYINITIAL> "#" { return PostCssTokenTypes.HASH_SIGN; }
<YYINITIAL> "<" { return PostCssTokenTypes.LT; }
<YYINITIAL> "<=" { return PostCssTokenTypes.LE; }
<YYINITIAL> ">=" { return PostCssTokenTypes.GE; }

<YYINITIAL> {CSS_IDENT}"(" { yybegin(CSS_FUNCTION); yypushback(yylength()); }
<YYINITIAL> {CSS_UNICODE_RANGE} { return CssElementTypes.CSS_UNICODE_RANGE; }
<YYINITIAL> {CSS_UNICODE_RANGE_MASK} { return CssElementTypes.CSS_UNICODE_RANGE; }
<YYINITIAL> {CSS_IDENT}{CSS_HACKS} { if(StringUtil.endsWith(yytext(), "\\0") || StringUtil.endsWith(yytext(), "\\9")) yypushback(2); else yypushback(3); return CssElementTypes.CSS_IDENT; }
<YYINITIAL> {CSS_IDENT} { return CssElementTypes.CSS_IDENT; }
<CSS_FUNCTION> {CSS_IDENT} { yybegin(YYINITIAL); return CssElementTypes.CSS_FUNCTION_TOKEN; }
<YYINITIAL> @{C}{H}{A}{R}{S}{E}{T} { return CssElementTypes.CSS_CHARSET_SYM; }
<YYINITIAL> @{F}{O}{N}{T}-{F}{A}{C}{E} { return CssElementTypes.CSS_FONTFACE_SYM; }
<YYINITIAL> @{K}{E}{Y}{F}{R}{A}{M}{E}{S}|@-{O}-{K}{E}{Y}{F}{R}{A}{M}{E}{S}|@-{M}{S}-{K}{E}{Y}{F}{R}{A}{M}{E}{S}|@-{W}{E}{B}{K}{I}{T}-{K}{E}{Y}{F}{R}{A}{M}{E}{S}|@-{M}{O}{Z}-{K}{E}{Y}{F}{R}{A}{M}{E}{S}|@-{K}{H}{T}{M}{L}-{K}{E}{Y}{F}{R}{A}{M}{E}{S} { 
  return CssElementTypes.CSS_KEYFRAMES_SYM; 
}
<YYINITIAL> {MARGIN_SYM} { return CssElementTypes.CSS_PAGE_MARGIN_SYM; }
<YYINITIAL> @{CSS_IDENT} { return CssElementTypes.CSS_ATKEYWORD; }
<YYINITIAL> {EL_EMBEDDMENT} { return CssElementTypes.CSS_IDENT; }
<YYINITIAL, CSS_URI> {POST_CSS_SIMPLE_VARIABLE_TOKEN} { return PostCssTokenTypes.POST_CSS_SIMPLE_VARIABLE_TOKEN; }
<YYINITIAL> [^] { return CssElementTypes.CSS_BAD_CHARACTER; }
