package org.angular2.lang.expr.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.stubs.JSLiteralExpressionStub
import com.intellij.lang.javascript.types.JSExpressionElementType
import com.intellij.lang.javascript.types.JSLiteralExpressionElementTypeImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import org.angular2.lang.expr.psi.impl.Angular2StringPartsLiteralExpressionImpl
import org.angular2.lang.expr.psi.impl.Angular2StringPartsLiteralExpressionStubImpl

class Angular2StringPartsLiteralExpressionType : JSLiteralExpressionElementTypeImpl("STRING_PARTS_LITERAL_EXPRESSION"),
                                                 JSExpressionElementType {

  override fun getExternalId(): String =
    Angular2StubElementTypes.EXTERNAL_ID_PREFIX + debugName

  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): JSLiteralExpressionStub =
    Angular2StringPartsLiteralExpressionStubImpl(dataStream, parentStub)

  override fun construct(node: ASTNode): PsiElement =
    Angular2StringPartsLiteralExpressionImpl(node)

  override fun createStub(psi: JSLiteralExpression, parentStub: StubElement<out PsiElement>?): JSLiteralExpressionStub =
    Angular2StringPartsLiteralExpressionStubImpl(psi, parentStub)

}