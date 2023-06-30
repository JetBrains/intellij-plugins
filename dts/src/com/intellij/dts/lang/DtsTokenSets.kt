package com.intellij.dts.lang

import com.intellij.psi.tree.TokenSet
import com.intellij.dts.lang.psi.DtsTypes

object DtsTokenSets {
    val comments = TokenSet.create(
        DtsTypes.COMMENT_EOL,
        DtsTypes.COMMENT_C,
    )

    val strings = TokenSet.create(
        DtsTypes.STRING_VALUE,
        DtsTypes.CHAR_VALUE,
    )

    val compilerDirectives = TokenSet.create(
        DtsTypes.V1,
        DtsTypes.PLUGIN,
        DtsTypes.DELETE_NODE,
        DtsTypes.DELETE_PROP,
        DtsTypes.OMIT_NODE,
        DtsTypes.MEMRESERVE,
        DtsTypes.BITS,
        DtsTypes.INCLUDE,
    )

    val ppDirectives = TokenSet.create(
        DtsTypes.PP_DEFINE,
        DtsTypes.PP_INCLUDE,
        DtsTypes.PP_IFDEF,
        DtsTypes.PP_IFNDEF,
        DtsTypes.PP_ENDIF,
        DtsTypes.PP_UNDEF,
    )

    val preprocessorStatements = TokenSet.create(
        DtsTypes.INCLUDE_STATEMENT,
        DtsTypes.PP_DEFINE_STATEMENT,
        DtsTypes.PP_INCLUDE_STATEMENT,
        DtsTypes.PP_IFDEF_STATEMENT,
        DtsTypes.PP_IFNDEF_STATEMENT,
        DtsTypes.PP_ENDIF_STATEMENT,
        DtsTypes.PP_UNDEF_STATEMENT,
    )

    val operators = TokenSet.create(
        DtsTypes.ADD,
        DtsTypes.SUB,
        DtsTypes.MUL,
        DtsTypes.DIV,
        DtsTypes.MOD,

        DtsTypes.AND,
        DtsTypes.OR,
        DtsTypes.XOR,
        DtsTypes.NOT,
        DtsTypes.LSH,
        DtsTypes.RSH,

        DtsTypes.L_AND,
        DtsTypes.L_OR,
        DtsTypes.L_NOT,

        DtsTypes.LES,
        DtsTypes.GRT,
        DtsTypes.LEQ,
        DtsTypes.GEQ,
        DtsTypes.EQ,
        DtsTypes.NEQ,
    )
}