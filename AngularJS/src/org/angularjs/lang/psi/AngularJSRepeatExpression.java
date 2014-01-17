package org.angularjs.lang.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.impl.JSExpressionImpl;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSRepeatExpression extends JSExpressionImpl {
  public AngularJSRepeatExpression(ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof AngularJSElementVisitor) {
      ((AngularJSElementVisitor)visitor).visitAngularJSRepeatExpression(this);
    } else {
      super.accept(visitor);
    }
  }
}
