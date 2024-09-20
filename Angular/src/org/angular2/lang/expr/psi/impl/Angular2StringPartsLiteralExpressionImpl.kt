package org.angular2.lang.expr.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.JSLiteralExpressionKind
import com.intellij.lang.javascript.psi.impl.JSLiteralExpressionImpl
import com.intellij.lang.javascript.psi.stubs.JSLiteralExpressionStub
import org.angular2.lang.expr.parser.Angular2StubElementTypes

class Angular2StringPartsLiteralExpressionImpl : JSLiteralExpressionImpl {

  constructor(node: ASTNode) : super(node)

  constructor(stub: JSLiteralExpressionStub) : super(stub, Angular2StubElementTypes.STRING_PARTS_LITERAL_EXPRESSION)

  override fun getExpressionKind(computeExactNumericKind: Boolean): JSLiteralExpressionKind =
    JSLiteralExpressionKind.QUOTED

}