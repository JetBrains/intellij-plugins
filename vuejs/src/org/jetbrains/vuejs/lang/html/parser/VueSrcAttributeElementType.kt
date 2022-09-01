// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.html.parser

import com.intellij.psi.impl.source.xml.stub.XmlAttributeStubImpl
import com.intellij.psi.impl.source.xml.stub.XmlStubBasedAttributeElementType
import com.intellij.psi.stubs.IndexSink
import com.intellij.util.PathUtil
import org.jetbrains.annotations.NonNls
import org.jetbrains.vuejs.index.VueUrlIndex
import org.jetbrains.vuejs.lang.html.VueLanguage

class VueSrcAttributeElementType : XmlStubBasedAttributeElementType("SRC_ATTRIBUTE", VueLanguage.INSTANCE) {
  override fun indexStub(stub: XmlAttributeStubImpl, sink: IndexSink) {
    stub.value
      ?.let { PathUtil.getFileName(it) }
      ?.takeIf { it.isNotBlank() }
      ?.let { sink.occurrence(VueUrlIndex.KEY, it) }
  }
}