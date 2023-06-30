package com.intellij.dts.lang.psi

sealed interface DtsNode : DtsStatement.Node {
    interface Root : DtsNode

    interface Sub : DtsNode
}