package com.intellij.dts.lang

import com.intellij.psi.tree.TokenSet
import com.intellij.dts.lang.psi.DtsTypes

object DtsTokenSets {
    val comments = TokenSet.create(
        DtsTypes.COMMENT_EOL,
        DtsTypes.COMMENT_C,

        // temporarily treat pp statements as comments
        DtsTypes.PP_STATEMENT
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