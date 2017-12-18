package org.angularjs.lang.psi;

import com.intellij.lang.javascript.psi.JSElementVisitor;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSElementVisitor extends JSElementVisitor {
  public void visitAngularJSRepeatExpression(AngularJSRepeatExpression repeatExpression) {
    visitJSExpression(repeatExpression);
  }

  public void visitMessageFormatExpression(AngularJSMessageFormatExpression expression) {
    visitJSExpression(expression);
  }
}
