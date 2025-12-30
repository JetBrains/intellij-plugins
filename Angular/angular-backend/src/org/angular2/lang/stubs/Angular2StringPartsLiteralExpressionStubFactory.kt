package org.angular2.lang.stubs

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.stubs.JSLiteralExpressionStub
import com.intellij.lang.javascript.stubs.factories.JSLiteralExpressionStubFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import org.angular2.lang.expr.parser.Angular2ElementTypes.STRING_PARTS_LITERAL_EXPRESSION
import org.angular2.lang.expr.psi.impl.Angular2StringPartsLiteralExpressionStubImpl

class Angular2StringPartsLiteralExpressionStubFactory : JSLiteralExpressionStubFactory(STRING_PARTS_LITERAL_EXPRESSION) {
  override fun createStub(psi: JSLiteralExpression, parentStub: StubElement<out PsiElement>?): JSLiteralExpressionStub =
    Angular2StringPartsLiteralExpressionStubImpl(psi, parentStub)
}