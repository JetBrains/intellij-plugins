package org.jetbrains.webstorm.lang.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import static org.jetbrains.webstorm.lang.psi.WebAssemblyTypes.*;

%%

%{
  public _WebAssemblyLexer() {
    this((java.io.Reader)null);
  }
%}

%class _WebAssemblyLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%unicode

%{
    private int blockCommentStart;
    private int blockCommentDepth;
%}

%state BLOCKCOMMENTST

WHITE_SPACE = [\s\t\n\r]+

// comments
LINECOMMENT = ;;.*
BLOCKCOMMENTSTART = "(;"
BLOCKCOMMENTEND = ";)"

// integers
SIGN = [+-]
DIGIT = [0-9]
HEXDIGIT = {DIGIT} | [a-fA-F]
NUM = {DIGIT} (_? {DIGIT})*
HEXNUM = {HEXDIGIT} (_? {HEXDIGIT})*
UN = {NUM} | 0x {HEXNUM}
SN = {SIGN} ({NUM} | 0x {HEXNUM})

// floating-point
FLOAT = {NUM} (\. {NUM}?)? ([Ee] {SIGN}? {NUM})?
HEXFLOAT = 0x {HEXNUM} (\. {HEXNUM}?)? ([Pp] {SIGN}? {NUM})?
FN = {SIGN}? ({FLOAT} | {HEXFLOAT} | inf | nan | nan:0x {HEXNUM})

// string
STRING = \" {STRINGELEM}* \"
STRINGELEM = \\ {HEXDIGIT} {HEXDIGIT} | \\u\{ {HEXNUM} \} | [ !#-\[\]-~:] | \\t | \\n | \\r | \\\" | \\' | \\\\

// identifiers
ID = \$ ({DIGIT} | [A-Za-z!#$%&′*+\-./:<=>?@\\\^_`|~])+

// types (updated)
NUMTYPE = [if] (32 | 64)
REFTYPE = "funcref" | "externref"
        // WebAssembly v1.0
        | "anyfunc"

// instructions (updated)
// control
CONTROLINSTR = unreachable | nop | return
CONTROLINSTR_IDX = br(_if)?
CALLINSTR = call
BRTABLEINSTR = br_table
CALLINDIRECTINSTR = call_indirect

// reference (new)
REFISNULLINST = ref\.is_null
REFNULLINSTR = ref\.null
REFFUNCINSTR = ref\.func

// parametric
PARAMETRICINSTR = drop | select

// variable
LOCALINSTR = local\.([gs]et | tee)
           // WebAssembly v1.0
           | [gs]et_local | tee_local
GLOBALINSTR = global\.[gs]et
            // WebAssembly v1.0
            | [gs]et_global

// table (new)
TABLEINSTR_IDX = table\.([gs]et | size | grow | fill)
TABLECOPYINSTR = table\.copy
TABLEINITINSTR = table\.init
ELEMDROPINSTR = elem\.drop

// memory (updated)
MEMORYINSTR = memory\.(size | grow | fill | copy)
            // WebAssembly v1.0
            | (current | grow)_memory
MEMORYINSTR_IDX = memory\.init | data\.drop
MEMORYINSTR_MEMARG = {NUMTYPE}\.(load | store)
                   | i32\.(load((8 | 16)_[su]) | store(8 | 16))
                   | i64\.(load((8 | 16 | 32)_[su]) | store(8 | 16 | 32))
                   // WebAssembly v1.0
                   | atomic\.wake
                   | i32\.atomic\.(wait | load((8 | 16)_[su])?
                                 | store(8 | 16)? | rmw((8 | 16)_u)?\.(add | sub | and | x?or | (cmp)?xchg))
                   | i64\.atomic\.(wait | load((8 | 16 | 32)_[su])?
                                 | store(8 | 16 | 32)? | rmw((8 | 16 | 32)_u)?\.(add | sub | and | x?or | (cmp)?xchg))

// numeric
ICONST = i(32 | 64)\.const
FCONST = f(32 | 64)\.const
NUMERICINSTR = i(32 | 64)\.(c[lt]z | popcnt | add | sub | mul | (div | rem | shr)_[su] | and | x?or | shl | rot[lr]
                                   | eqz? | ne | [lg][te]_[su] | trunc_((f | sat)(32 | 64)_[su]))
             | i32\.(wrap_i64 | extend(8 | 16)_s)
             | i64\.(extend_i32[su] | extend(8 | 16 | 32)_s)
             | f (32 | 64)\.(abs | neg | ceil | floor | trunc | nearest | sqrt | add | sub | mul | div | min | max
                                 | copysign | eq | ne | [lg][te] | convert_i(32 | 64)_[su])
             | f32\.demote_f64 | f64\.promote_f32
             | i32\.reinterpret_f32 | i64\.reinterpret_f64 | f32\.reinterpret_i32 | f64\.reinterpret_i64
             // WebAssembly v1.0
             | i32\.wrap\/i64
             | i(32 | 64)\.(trunc_[su]\/f | convert_[su]\/i)(32 | 64)
             | f(32 | 64)\.convert_[su]\/i(32 | 64)
             | i64\.extend_[su]\/i32
             | f32\.demote\/f64 | f64\.promote\/f32
             | i32\.reinterpret\/f32 | i64\.reinterpret\/f64 | f32\.reinterpret\/i32 | f64\.reinterpret\/i64


%%

<BLOCKCOMMENTST> {
    {BLOCKCOMMENTSTART} {
        blockCommentDepth++;
    }

    <<EOF>> {
        yybegin(YYINITIAL);
        zzStartRead = blockCommentStart;
        return BLOCK_COMMENT;
    }

    {BLOCKCOMMENTEND} {
        if (blockCommentDepth > 0) {
            blockCommentDepth--;
        } else {
            yybegin(YYINITIAL);
            zzStartRead = blockCommentStart;
            return BLOCK_COMMENT;
        }
    }

    [^] {}
}

<YYINITIAL> {
    {WHITE_SPACE}               { return TokenType.WHITE_SPACE; }
    {LINECOMMENT}               { return LINE_COMMENT; }
    {BLOCKCOMMENTSTART}         { yybegin(BLOCKCOMMENTST); blockCommentDepth = 0; blockCommentStart = getTokenStart(); }

    // types
    {NUMTYPE}                   { return NUMTYPE; }
    {REFTYPE}                   { return REFTYPE; }
    "extern"                    { return EXTERNKEY; }
    "func"                      { return FUNCKEY; }
    "param"                     { return PARAMKEY; }
    "result"                    { return RESULTKEY; }
    "mut"                       { return MUTKEY; }

    // instructions
    "block"                     { return BLOCKKEY; }
    "loop"                      { return LOOPKEY; }
    "end"                       { return ENDKEY; }
    "if"                        { return IFKEY; }
    "then"                      { return THENKEY; }
    "else"                      { return ELSEKEY; }

    {CONTROLINSTR}              { return CONTROLINSTR; }
    {CONTROLINSTR_IDX}          { return CONTROLINSTR_IDX; }
    {CALLINSTR}                 { return CALLINSTR; }
    {BRTABLEINSTR}              { return BRTABLEINSTR; }
    {CALLINDIRECTINSTR}         { return CALLINDIRECTINSTR; }

    {REFISNULLINST}             { return REFISNULLINST; }
    {REFNULLINSTR}              { return REFNULLINSTR; }
    {REFFUNCINSTR}              { return REFFUNCINSTR; }

    {PARAMETRICINSTR}           { return PARAMETRICINSTR; }

    {LOCALINSTR}                { return LOCALINSTR; }
    {GLOBALINSTR}               { return GLOBALINSTR; }

    {TABLEINSTR_IDX}            { return TABLEINSTR_IDX; }
    {TABLECOPYINSTR}            { return TABLECOPYINSTR; }
    {TABLEINITINSTR}            { return TABLEINITINSTR; }
    {ELEMDROPINSTR}             { return ELEMDROPINSTR; }

    "offset="                   { return OFFSETEQKEY; }
    "align="                    { return ALIGNEQKEY; }
    {MEMORYINSTR}               { return MEMORYINSTR; }
    {MEMORYINSTR_IDX}           { return MEMORYINSTR_IDX; }
    {MEMORYINSTR_MEMARG}        { return MEMORYINSTR_MEMARG; }

    {FCONST}                    { return FCONST; }
    {ICONST}                    { return ICONST; }
    {NUMERICINSTR}              { return NUMERICINSTR; }

    // modules
    "type"                      { return TYPEKEY; }
    "import"                    { return IMPORTKEY; }
    "table"                     { return TABLEKEY; }
    "memory"                    { return MEMORYKEY; }
    "global"                    { return GLOBALKEY; }
    "local"                     { return LOCALKEY; }
    "export"                    { return EXPORTKEY; }
    "start"                     { return STARTKEY; }
    "elem"                      { return ELEMKEY; }
    "offset"                    { return OFFSETKEY; }
    "declare"                   { return DECLAREKEY; }
    "item"                      { return ITEMKEY; }
    "data"                      { return DATAKEY; }
    "module"                    { return MODULEKEY; }

    // other tokens
    {UN}                        { return UNSIGNED; }
    {SN}                        { return SIGNED; }
    {FN}                        { return FLOAT; }
    {STRING}                    { return STRING; }
    {ID}                        { return IDENTIFIER; }
    "("                         { return LPAR; }
    ")"                         { return RPAR; }

    [^()$\s\t\n\r=]+            { return BAD_TOKEN; }

    [^]                         { return TokenType.BAD_CHARACTER; }
}
