package com.intellij.dts.lang.stubs

import com.intellij.dts.lang.psi.DtsRootNode
import com.intellij.dts.lang.psi.impl.DtsRootNodeImpl
import com.intellij.psi.stubs.*

class DtsRootNodeStub(
    parent: StubElement<*>?,
    elementType: IStubElementType<*, *>,
    val labels: List<String>,
) : StubBase<DtsRootNode>(parent, elementType) {
    class Type(debugName: String) : DtsStubElementType<DtsRootNodeStub, DtsRootNode>(debugName) {
        override fun createPsi(stub: DtsRootNodeStub): DtsRootNode = DtsRootNodeImpl(stub, this)

        override fun createStub(psi: DtsRootNode, parentStub: StubElement<*>?): DtsRootNodeStub {
            return DtsRootNodeStub(parentStub, this, psi.dtsLabels)
        }

        override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): DtsRootNodeStub {
            return DtsRootNodeStub(parentStub, this, dataStream.readUTFList())
        }

        override fun serialize(stub: DtsRootNodeStub, dataStream: StubOutputStream) {
            dataStream.writeUTFList(stub.labels)
        }

        override fun indexStub(stub: DtsRootNodeStub, sink: IndexSink) {
            for (label in stub.labels) {
                sink.occurrence(DTS_NODE_LABEL_INDEX, label)
            }
        }
    }
}