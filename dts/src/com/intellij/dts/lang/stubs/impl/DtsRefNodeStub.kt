package com.intellij.dts.lang.stubs.impl

import com.intellij.dts.lang.psi.DtsRefNode
import com.intellij.dts.lang.psi.impl.DtsRefNodeImpl
import com.intellij.dts.lang.stubs.DTS_NODE_LABEL_INDEX
import com.intellij.dts.lang.stubs.DtsStubElementType
import com.intellij.dts.lang.stubs.readUTFList
import com.intellij.dts.lang.stubs.writeUTFList
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream

class DtsRefNodeStub(
  parent: StubElement<*>?,
  elementType: IStubElementType<*, *>,
  val labels: List<String>,
) : StubBase<DtsRefNode>(parent, elementType) {
  class Type(debugName: String) : DtsStubElementType<DtsRefNodeStub, DtsRefNode>(debugName) {
    override fun createPsi(stub: DtsRefNodeStub): DtsRefNode = DtsRefNodeImpl(stub, this)

    override fun createStub(psi: DtsRefNode, parentStub: StubElement<*>?): DtsRefNodeStub {
      return DtsRefNodeStub(parentStub, this, psi.dtsLabels)
    }

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): DtsRefNodeStub {
      return DtsRefNodeStub(parentStub, this, dataStream.readUTFList())
    }

    override fun serialize(stub: DtsRefNodeStub, dataStream: StubOutputStream) {
      dataStream.writeUTFList(stub.labels)
    }

    override fun indexStub(stub: DtsRefNodeStub, sink: IndexSink) {
      for (label in stub.labels) {
        sink.occurrence(DTS_NODE_LABEL_INDEX, label)
      }
    }
  }
}