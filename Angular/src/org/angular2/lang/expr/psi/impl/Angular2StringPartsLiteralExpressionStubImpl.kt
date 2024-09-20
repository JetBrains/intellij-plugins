package org.angular2.lang.expr.psi.impl

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.stubs.impl.JSLiteralExpressionStubImpl
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import org.angular2.lang.expr.parser.Angular2StubElementTypes

class Angular2StringPartsLiteralExpressionStubImpl : JSLiteralExpressionStubImpl {

  constructor(expr: JSLiteralExpression?, parent: StubElement<*>?)
    : super(expr, parent, Angular2StubElementTypes.STRING_PARTS_LITERAL_EXPRESSION)

  constructor(dataStream: StubInputStream, parentStub: StubElement<*>?)
    : super(dataStream, parentStub, Angular2StubElementTypes.STRING_PARTS_LITERAL_EXPRESSION)

  override fun createPsi(): JSLiteralExpression {
    return Angular2StringPartsLiteralExpressionImpl(this)
  }

}