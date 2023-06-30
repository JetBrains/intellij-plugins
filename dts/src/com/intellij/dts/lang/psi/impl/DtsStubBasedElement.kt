package com.intellij.dts.lang.psi.impl

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement

abstract class DtsStubBasedElement<T: StubElement<*>> : StubBasedPsiElementBase<T> {
    constructor(node: ASTNode) : super(node)

    constructor(stub: T, elementType: IStubElementType<*, *>) : super(stub, elementType)

    override fun toString(): String = "${this::class.simpleName}($elementType)"
}
