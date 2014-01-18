package org.angularjs.lang.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.impl.JSExpressionImpl;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSFilterExpression extends JSExpressionImpl {
  public AngularJSFilterExpression(ASTNode node) {
    super(node);
  }
}
