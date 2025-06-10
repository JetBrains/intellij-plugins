// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.stubs.serializers

import com.intellij.lang.stubs.XmlStubBasedAttributeStubSerializer
import com.intellij.psi.impl.source.xml.stub.XmlAttributeStubImpl
import com.intellij.psi.stubs.IndexSink
import org.jetbrains.vuejs.index.VUE_ID_INDEX_KEY
import org.jetbrains.vuejs.lang.html.parser.VueStubElementTypes.SCRIPT_ID_ATTRIBUTE

class VueScriptIdAttributeStubSerializer : XmlStubBasedAttributeStubSerializer(SCRIPT_ID_ATTRIBUTE) {
  override fun indexStub(stub: XmlAttributeStubImpl, sink: IndexSink) {
    stub.value?.let { sink.occurrence(VUE_ID_INDEX_KEY, it) }
  }
}