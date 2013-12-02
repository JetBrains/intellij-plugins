package org.angularjs.index;

import com.intellij.lang.javascript.index.FrameworkIndexingHandler;
import com.intellij.lang.javascript.index.JSSymbolVisitor;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;

/**
 * @author Dennis.Ushakov
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
        if (argument instanceof JSLiteralExpression && ((JSLiteralExpression)argument).isQuotedLiteral()) {
          visitor.storeAdditionalData(DIRECTIVE, getAttributeName(argument), argument.getTextOffset());
        }
      }
    }
  }

  private static String getAttributeName(PsiElement argument) {
    final String[] split = StringUtil.unquoteString(argument.getText()).split("(?=[A-Z])");
    for (int i = 0; i < split.length; i++) {
      split[i] = StringUtil.decapitalize(split[i]);
    }
    return StringUtil.join(split, "-");
  }
}
