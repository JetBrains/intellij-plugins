package org.angularjs.index

import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.openapi.util.text.StringUtil
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
fun getExprReferencedFileUrl(expression: JSExpression?): String? {
    if (expression is JSReferenceExpression) {
        for (resolvedElement in AngularIndexUtil.resolveLocally(expression)) {
            if (resolvedElement is ES6ImportedBinding) {
                val from = resolvedElement.declaration?.fromClause
                if (from != null) {
                    return from.referenceText?.let { StringUtil.unquoteString(it) }
                }
            }
        }
    }
    else if (expression is JSCallExpression) {
        val referenceExpression = expression.methodExpression as? JSReferenceExpression
        val arguments = expression.arguments
        if (arguments.size == 1
            && arguments[0] is JSLiteralExpression
            && (arguments[0] as JSLiteralExpression).isQuotedLiteral
            && referenceExpression != null
            && referenceExpression.qualifier == null
            && "require" == referenceExpression.referenceName) {
            return (arguments[0] as JSLiteralExpression).stringValue
        }
    }
    return null
}