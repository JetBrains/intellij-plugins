// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.stub

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.tree.CompositeElement
import com.intellij.psi.stubs.*
import com.intellij.psi.tree.ICompositeElementType
import org.angular2.lang.html.Angular2HtmlLanguage
import org.angular2.lang.html.psi.Angular2HtmlNgContentSelector
import org.angular2.lang.html.psi.impl.Angular2HtmlNgContentSelectorImpl
import org.angular2.lang.html.stub.impl.Angular2HtmlNgContentSelectorStubImpl
import org.jetbrains.annotations.NonNls
import java.io.IOException

class Angular2HtmlNgContentSelectorElementType
  : IStubElementType<Angular2HtmlNgContentSelectorStub, Angular2HtmlNgContentSelector>("NG_CONTENT_SELECTOR",
                                                                                       Angular2HtmlLanguage.INSTANCE),
    ICompositeElementType {
  override fun toString(): @NonNls String {
    return Angular2HtmlStubElementTypes.EXTERNAL_ID_PREFIX + super.getDebugName()
  }

  override fun getExternalId(): String {
    return toString()
  }

  @Throws(IOException::class)
  override fun serialize(stub: Angular2HtmlNgContentSelectorStub, dataStream: StubOutputStream) {
    (stub as Angular2HtmlNgContentSelectorStubImpl).serialize(dataStream)
  }

  @Throws(IOException::class)
  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): Angular2HtmlNgContentSelectorStub {
    return Angular2HtmlNgContentSelectorStubImpl(parentStub, dataStream)
  }

  override fun indexStub(stub: Angular2HtmlNgContentSelectorStub, sink: IndexSink) {
    (stub as Angular2HtmlNgContentSelectorStubImpl).index(sink)
  }

  override fun createPsi(stub: Angular2HtmlNgContentSelectorStub): Angular2HtmlNgContentSelector {
    return (stub as Angular2HtmlNgContentSelectorStubImpl).createPsi()
  }

  override fun createStub(psi: Angular2HtmlNgContentSelector, parentStub: StubElement<*>?): Angular2HtmlNgContentSelectorStub {
    return Angular2HtmlNgContentSelectorStubImpl(psi, parentStub)
  }

  override fun createCompositeNode(): ASTNode {
    return CompositeElement(Angular2HtmlStubElementTypes.NG_CONTENT_SELECTOR)
  }

  fun createPsi(node: ASTNode): Angular2HtmlNgContentSelector {
    return Angular2HtmlNgContentSelectorImpl(node)
  }
}