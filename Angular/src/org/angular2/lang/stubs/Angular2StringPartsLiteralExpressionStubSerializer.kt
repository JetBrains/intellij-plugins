package org.angular2.lang.stubs

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.stubs.JSLiteralExpressionStub
import com.intellij.lang.javascript.stubs.serializers.JSStubSerializer
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import org.angular2.lang.expr.parser.Angular2ElementTypes.STRING_PARTS_LITERAL_EXPRESSION
import org.angular2.lang.expr.psi.impl.Angular2StringPartsLiteralExpressionStubImpl

class Angular2StringPartsLiteralExpressionStubSerializer : JSStubSerializer<JSLiteralExpressionStub, JSLiteralExpression>(STRING_PARTS_LITERAL_EXPRESSION) {
  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): JSLiteralExpressionStub =
    Angular2StringPartsLiteralExpressionStubImpl(dataStream, parentStub)
}