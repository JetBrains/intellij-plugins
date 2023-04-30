package com.intellij.dts.lang

import com.intellij.psi.tree.IElementType

class DtsTokenType(debugName: String) : IElementType(debugName, DtsLanguage) {
    override fun toString(): String {
        return "DtsTokenType." + super.toString()
    }
}