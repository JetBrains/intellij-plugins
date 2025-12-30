package org.angular2.lang.expr.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.types.JSExpressionElementType
import com.intellij.lang.javascript.types.JSLiteralExpressionElementTypeImpl
import com.intellij.psi.PsiElement
import org.angular2.lang.expr.psi.impl.Angular2StringPartsLiteralExpressionImpl

class Angular2StringPartsLiteralExpressionType : JSLiteralExpressionElementTypeImpl("STRING_PARTS_LITERAL_EXPRESSION"),
                                                 JSExpressionElementType {

  override fun toString(): String =
    Angular2ElementTypes.EXTERNAL_ID_PREFIX + debugName

  override fun construct(node: ASTNode): PsiElement =
    Angular2StringPartsLiteralExpressionImpl(node)

}