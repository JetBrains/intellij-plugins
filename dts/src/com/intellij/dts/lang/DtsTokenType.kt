package com.intellij.dts.lang

import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.psi.tree.IElementType

class DtsTokenType(debugName: String) : IElementType(debugName, DtsLanguage) {
    override fun toString(): String {
        return when (this) {
            DtsTypes.LABEL -> "label"
            DtsTypes.NAME -> "name"
            DtsTypes.INT_VALUE -> "integer"
            DtsTypes.BYTE_VALUE -> "byte"
            DtsTypes.STRING_VALUE -> "string"
            DtsTypes.CHAR_VALUE -> "char"
            DtsTypes.PATH -> "path"

            DtsTypes.SEMICOLON -> ";"
            DtsTypes.ASSIGN -> "="
            DtsTypes.COMMA -> ","
            DtsTypes.SLASH -> "/"
            DtsTypes.HANDLE -> "&"
            DtsTypes.DQUOTE -> "\""
            DtsTypes.SQUOTE -> "'"

            // compiler directives
            DtsTypes.V1 -> "/dts-v1/"
            DtsTypes.PLUGIN -> "/plugin/"
            DtsTypes.INCLUDE -> "/include/"
            DtsTypes.MEMRESERVE -> "/memreserve/"
            DtsTypes.DELETE_NODE -> "/delete-node/"
            DtsTypes.DELETE_PROP -> "/delete-property/"
            DtsTypes.OMIT_NODE -> "/omit-if-no-ref/"
            DtsTypes.BITS -> "/bits/"

            // braces
            DtsTypes.LBRACE -> "{"
            DtsTypes.RBRACE -> "}"
            DtsTypes.LPAREN -> "("
            DtsTypes.RPAREN -> ")"
            DtsTypes.LBRAC -> "["
            DtsTypes.RBRAC -> "]"
            DtsTypes.LANGL -> "<"
            DtsTypes.RANGL -> ">"

            // expressions
            DtsTypes.ADD -> "+"
            DtsTypes.SUB -> "-"
            DtsTypes.MUL -> "*"
            DtsTypes.DIV -> "/"
            DtsTypes.MOD -> "%"
            DtsTypes.AND -> "&"

            DtsTypes.OR -> "|"
            DtsTypes.XOR -> "^"
            DtsTypes.NOT -> "~"
            DtsTypes.LSH -> "<<"
            DtsTypes.RSH -> ">>"

            DtsTypes.L_AND -> "&&"
            DtsTypes.L_OR -> "||"
            DtsTypes.L_NOT -> "!"

            DtsTypes.LES -> "<"
            DtsTypes.GRT -> ">"
            DtsTypes.LEQ -> "<="
            DtsTypes.GEQ -> ">="
            DtsTypes.EQ -> "=="
            DtsTypes.NEQ -> "!="
            DtsTypes.COLON -> ":"
            DtsTypes.TERNARY -> "?"

            // c preprocessor
            DtsTypes.PP_INCLUDE -> "#include"
            DtsTypes.PP_IFDEF -> "#ifdef"
            DtsTypes.PP_IFNDEF -> "#ifndef"
            DtsTypes.PP_ENDIF -> "#endif"
            DtsTypes.PP_DEFINE -> "#define"
            DtsTypes.PP_UNDEF -> "#undef"

            DtsTypes.PP_SYMBOL -> "identifier"
            DtsTypes.PP_PATH -> "path"
            DtsTypes.PP_DEFINE_VALUE -> "value"

            DtsTypes.PP_LANGLE -> "<"
            DtsTypes.PP_RANGLE -> ">"
            DtsTypes.PP_DQUOTE -> "\""

            else -> super.toString()
        }
    }
}