package org.angularjs.index;

import com.intellij.lang.javascript.index.FrameworkIndexingHandler;
import com.intellij.lang.javascript.index.JSSymbolVisitor;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.util.text.StringUtil;

/**
 * Created by denofevil on 27/11/13.
 */
public class AngularJSIndexingHandler extends FrameworkIndexingHandler {
    public static final String DIRECTIVE = "AngularJS.Directive";
    @Override
    public void processCallExpression(JSCallExpression callExpression, JSSymbolVisitor visitor) {
        JSReferenceExpression callee = (JSReferenceExpression)callExpression.getMethodExpression();
        JSExpression qualifier = callee.getQualifier();

        if ("directive".equals(callee.getReferencedName()) && qualifier != null) {
            JSExpression[] arguments = callExpression.getArguments();
            if (arguments.length > 0) {
                JSExpression argument = arguments[0];
                if (argument instanceof JSLiteralExpression && ((JSLiteralExpression) argument).isQuotedLiteral()) {
                    visitor.storeAdditionalData(DIRECTIVE, StringUtil.unquoteString(argument.getText()),
                                            argument.getTextOffset());
                }
            }
        }
    }
}
