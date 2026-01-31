package org.angular2.lang.stubs

import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.psi.stubs.StubSerializer
import org.angular2.lang.html.parser.Angular2HtmlElementTypes.NG_CONTENT_SELECTOR
import org.angular2.lang.html.stub.Angular2HtmlNgContentSelectorStub
import org.angular2.lang.html.stub.impl.Angular2HtmlNgContentSelectorStubImpl

class Angular2HtmlNgContentSelectorStubSerializer : StubSerializer<Angular2HtmlNgContentSelectorStub> {
  override fun getExternalId(): String =
    NG_CONTENT_SELECTOR.toString()

  override fun serialize(stub: Angular2HtmlNgContentSelectorStub, dataStream: StubOutputStream) {
    (stub as Angular2HtmlNgContentSelectorStubImpl).serialize(dataStream)
  }

  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): Angular2HtmlNgContentSelectorStub =
    Angular2HtmlNgContentSelectorStubImpl(parentStub, dataStream)

  override fun indexStub(stub: Angular2HtmlNgContentSelectorStub, sink: IndexSink) {
    (stub as Angular2HtmlNgContentSelectorStubImpl).index(sink)
  }
}