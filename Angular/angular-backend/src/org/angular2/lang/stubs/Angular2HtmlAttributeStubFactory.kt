package org.angular2.lang.stubs

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubElementFactory
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlTokenType
import org.angular2.isCustomCssPropertyBinding
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.psi.impl.Angular2HtmlBoundAttributeImpl
import org.angular2.lang.html.stub.impl.Angular2HtmlBoundAttributeStubImpl

class Angular2HtmlAttributeStubFactory(
  val elementType: IElementType,
  private val fromStubConstructor: (stub: Angular2HtmlBoundAttributeStubImpl, elementType: IElementType) -> Angular2HtmlBoundAttributeImpl
) : StubElementFactory<Angular2HtmlBoundAttributeStubImpl, Angular2HtmlBoundAttributeImpl> {

  override fun createStub(psi: Angular2HtmlBoundAttributeImpl, parentStub: StubElement<out PsiElement>?): Angular2HtmlBoundAttributeStubImpl =
    Angular2HtmlBoundAttributeStubImpl(psi, parentStub, elementType)

  override fun createPsi(stub: Angular2HtmlBoundAttributeStubImpl): Angular2HtmlBoundAttributeImpl =
    fromStubConstructor(stub, elementType)

  override fun shouldCreateStub(node: ASTNode): Boolean {
    val name = node.firstChildNode?.takeIf { it.elementType == XmlTokenType.XML_NAME }?.text
               ?: return false
    val info = Angular2AttributeNameParser.parse(name)
    return isCustomCssPropertyBinding(info)
  }
}