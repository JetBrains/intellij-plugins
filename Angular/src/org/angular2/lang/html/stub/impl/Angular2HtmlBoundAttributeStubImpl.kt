package org.angular2.lang.html.stub.impl

import com.intellij.psi.impl.source.xml.stub.XmlAttributeStub
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.psi.tree.IElementType
import com.intellij.util.io.StringRef
import org.angular2.lang.html.psi.impl.Angular2HtmlBoundAttributeImpl
import java.io.IOException

class Angular2HtmlBoundAttributeStubImpl : StubBase<Angular2HtmlBoundAttributeImpl>, XmlAttributeStub<Angular2HtmlBoundAttributeImpl> {

  private val name: String
  private val value: String?

  constructor(
    parent: StubElement<*>?,
    dataStream: StubInputStream,
    elementType: IElementType,
  ) : super(parent, elementType) {
    name = (StringRef.toString(dataStream.readName())) ?: ""
    value = StringRef.toString(dataStream.readName());
  }

  constructor(
    psi: Angular2HtmlBoundAttributeImpl,
    parent: StubElement<*>?,
    elementType: IElementType,
  ) : super(parent, elementType) {
    name = psi.getName()
    value = psi.getValue()
  }

  @Throws(IOException::class)
  fun serialize(stream: StubOutputStream) {
    stream.writeName(name)
    stream.writeName(value)
  }

  fun getName(): String = name

  fun getValue(): String? = value

}