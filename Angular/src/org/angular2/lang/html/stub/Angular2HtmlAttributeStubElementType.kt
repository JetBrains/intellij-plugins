package org.angular2.lang.html.stub

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.xml.stub.XmlStubBasedElementType
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.psi.xml.IXmlAttributeElementType
import com.intellij.psi.xml.XmlTokenType
import org.angular2.index.Angular2CustomCssPropertyInHtmlAttributeIndexKey
import org.angular2.isCustomCssPropertyBinding
import org.angular2.lang.html.Angular2HtmlLanguage
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.psi.impl.Angular2HtmlBoundAttributeImpl
import org.angular2.lang.html.stub.impl.Angular2HtmlBoundAttributeStubImpl
import org.jetbrains.annotations.NonNls

internal class Angular2HtmlAttributeStubElementType(
  typeName: @NonNls String,
  private val fromStubConstructor: (stub: Angular2HtmlBoundAttributeStubImpl, nodeType: Angular2HtmlAttributeStubElementType) -> Angular2HtmlBoundAttributeImpl,
  private val fromAstConstructor: (node: ASTNode) -> Angular2HtmlBoundAttributeImpl,
) : XmlStubBasedElementType<Angular2HtmlBoundAttributeStubImpl, Angular2HtmlBoundAttributeImpl>(typeName, Angular2HtmlLanguage),
    IXmlAttributeElementType {

  override fun shouldCreateStub(node: ASTNode): Boolean {
    val name = node.firstChildNode?.takeIf { it.elementType == XmlTokenType.XML_NAME }?.text
               ?: return false
    val info = Angular2AttributeNameParser.parse(name)
    return isCustomCssPropertyBinding(info)
  }

  override fun indexStub(stub: Angular2HtmlBoundAttributeStubImpl, sink: IndexSink) {
    val info = Angular2AttributeNameParser.parse(stub.getName())
    if (isCustomCssPropertyBinding(info)) {
      sink.occurrence(Angular2CustomCssPropertyInHtmlAttributeIndexKey, stub.getName())
    }
  }

  override fun createPsi(node: ASTNode): Angular2HtmlBoundAttributeImpl =
    fromAstConstructor(node)

  override fun createPsi(stub: Angular2HtmlBoundAttributeStubImpl): Angular2HtmlBoundAttributeImpl =
    fromStubConstructor(stub, this)

  override fun createStub(psi: Angular2HtmlBoundAttributeImpl, parentStub: StubElement<out PsiElement>?): Angular2HtmlBoundAttributeStubImpl =
    Angular2HtmlBoundAttributeStubImpl(psi, parentStub, this)

  override fun getExternalId(): String =
    Angular2HtmlStubElementTypes.EXTERNAL_ID_PREFIX + super.getDebugName()

  override fun getDebugName(): @NonNls String =
    "NG:" + super.getDebugName()

  override fun toString(): String =
    debugName

  override fun serialize(stub: Angular2HtmlBoundAttributeStubImpl, dataStream: StubOutputStream) {
    stub.serialize(dataStream)
  }

  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): Angular2HtmlBoundAttributeStubImpl =
    Angular2HtmlBoundAttributeStubImpl(parentStub, dataStream, this)
}