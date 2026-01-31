package com.intellij.dts.lang.stubs.impl

import com.intellij.dts.lang.psi.DtsRootNode
import com.intellij.dts.lang.psi.impl.DtsRootNodeImpl
import com.intellij.dts.lang.stubs.DtsStubElementType
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream

class DtsRootNodeStub(
  parent: StubElement<*>?,
  elementType: IStubElementType<*, *>,
) : StubBase<DtsRootNode>(parent, elementType) {
  class Type(debugName: String) : DtsStubElementType<DtsRootNodeStub, DtsRootNode>(debugName) {
    override fun createPsi(stub: DtsRootNodeStub): DtsRootNode = DtsRootNodeImpl(stub, this)

    override fun createStub(psi: DtsRootNode, parentStub: StubElement<*>?): DtsRootNodeStub {
      return DtsRootNodeStub(parentStub, this)
    }

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): DtsRootNodeStub {
      return DtsRootNodeStub(parentStub, this)
    }

    override fun serialize(stub: DtsRootNodeStub, dataStream: StubOutputStream) {}

    override fun indexStub(stub: DtsRootNodeStub, sink: IndexSink) {}
  }
}