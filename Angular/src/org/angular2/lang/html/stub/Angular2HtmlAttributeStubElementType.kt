package org.angular2.lang.html.stub

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.xml.stub.XmlStubBasedElementType
import com.intellij.psi.xml.IXmlAttributeElementType
import org.angular2.lang.html.Angular2HtmlLanguage
import org.angular2.lang.html.psi.impl.Angular2HtmlBoundAttributeImpl
import org.jetbrains.annotations.NonNls

class Angular2HtmlAttributeStubElementType(
  typeName: @NonNls String,
  private val fromAstConstructor: (node: ASTNode) -> Angular2HtmlBoundAttributeImpl,
) : XmlStubBasedElementType<Angular2HtmlBoundAttributeImpl>(typeName, Angular2HtmlLanguage),
    IXmlAttributeElementType {

  override fun createPsi(node: ASTNode): Angular2HtmlBoundAttributeImpl =
    fromAstConstructor(node)

  override fun toString(): @NonNls String =
    "NG:" + super.getDebugName()
}