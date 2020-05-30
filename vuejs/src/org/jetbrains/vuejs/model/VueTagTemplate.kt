// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.psi.impl.source.xml.stub.XmlTagStub
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.xml.XmlTag

class VueTagTemplate(override val source: XmlTag) : VueTemplate<XmlTag> {

  override fun safeVisitTags(visitor: (XmlTag) -> Unit) {
    if (source is StubBasedPsiElementBase<*>) {
      source.stub?.let { stub ->
        stub.childrenStubs.forEach { visitStubsRecursively(it, visitor) }
        return
      }
    }
    super.safeVisitTags(visitor)
  }

  private fun visitStubsRecursively(stub: StubElement<*>, visitor: (XmlTag) -> Unit) {
    if (stub is XmlTagStub<*>) {
      visitor(stub.psi)
      stub.childrenStubs.forEach { visitStubsRecursively(it, visitor) }
    }
  }

  override fun equals(other: Any?): Boolean {
    return other is VueTagTemplate
           && other.source == source
  }

  override fun hashCode(): Int {
    return source.hashCode()
  }

}
