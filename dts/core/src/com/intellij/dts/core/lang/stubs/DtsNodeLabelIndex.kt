package com.intellij.dts.core.lang.stubs

import com.intellij.dts.core.lang.psi.DtsNode
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey

val DTS_NODE_LABEL_INDEX: StubIndexKey<String, DtsNode> = StubIndexKey.createIndexKey("dts.label_index")

class DtsNodeLabelIndex : StringStubIndexExtension<DtsNode>() {
  override fun getVersion(): Int = DtsFileStub.Type.stubVersion

  override fun getKey(): StubIndexKey<String, DtsNode> = DTS_NODE_LABEL_INDEX

  override fun traceKeyHashToVirtualFileMapping(): Boolean = true
}