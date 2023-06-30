package com.intellij.dts.lang.stubs

import com.intellij.psi.tree.IElementType

interface DtsStubElementTypes {
    companion object {
        val ROOT_NODE = DtsRootNodeStub.Type("ROOT_NODE")
        val SUB_NODE = DtsSubNodeStub.Type("SUB_NODE")

        fun factory(name: String): IElementType = when(name) {
            "ROOT_NODE" -> ROOT_NODE
            "SUB_NODE" -> SUB_NODE
            else -> throw IllegalArgumentException("Unknown name: $name")
        }
    }
}

