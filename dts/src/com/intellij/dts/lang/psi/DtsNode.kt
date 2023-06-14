package com.intellij.dts.lang.psi

sealed interface DtsNode : DtsContainer, DtsStatement.Node {
    interface Root : DtsNode

    interface Sub : DtsNode
}