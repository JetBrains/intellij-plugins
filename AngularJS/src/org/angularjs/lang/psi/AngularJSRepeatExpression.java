package org.angularjs.lang.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.impl.JSExpressionImpl;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSRepeatExpression extends JSExpressionImpl {
  public AngularJSRepeatExpression(ASTNode node) {
    super(node);
  }
}
