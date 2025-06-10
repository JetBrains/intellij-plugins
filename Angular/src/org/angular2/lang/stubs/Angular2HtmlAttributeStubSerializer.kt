package org.angular2.lang.stubs

import com.intellij.lang.stubs.XmlStubBasedStubSerializer
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.psi.tree.IElementType
import org.angular2.index.Angular2CustomCssPropertyInHtmlAttributeIndexKey
import org.angular2.isCustomCssPropertyBinding
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.stub.Angular2HtmlStubElementTypes
import org.angular2.lang.html.stub.impl.Angular2HtmlBoundAttributeStubImpl

class Angular2HtmlAttributeStubSerializer(elementType: IElementType) : XmlStubBasedStubSerializer<Angular2HtmlBoundAttributeStubImpl>(elementType) {
  override fun getExternalId(): String {
    return Angular2HtmlStubElementTypes.EXTERNAL_ID_PREFIX + super.externalId
  }

  override fun serialize(stub: Angular2HtmlBoundAttributeStubImpl, dataStream: StubOutputStream) {
    stub.serialize(dataStream)
  }

  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): Angular2HtmlBoundAttributeStubImpl =
    Angular2HtmlBoundAttributeStubImpl(parentStub, dataStream, elementType)

  override fun indexStub(stub: Angular2HtmlBoundAttributeStubImpl, sink: IndexSink) {
    val info = Angular2AttributeNameParser.parse(stub.getName())
    if (isCustomCssPropertyBinding(info)) {
      sink.occurrence(Angular2CustomCssPropertyInHtmlAttributeIndexKey, stub.getName())
    }
  }
}