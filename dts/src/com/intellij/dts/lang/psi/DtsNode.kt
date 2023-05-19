package com.intellij.dts.lang.psi

sealed interface DtsNode : DtsContainer {
    interface Root : DtsNode

    interface Sub : DtsNode
}