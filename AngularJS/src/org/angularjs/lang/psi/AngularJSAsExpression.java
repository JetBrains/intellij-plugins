package org.angularjs.lang.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.JSDefinitionExpression;
import com.intellij.lang.javascript.psi.impl.JSExpressionImpl;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSAsExpression extends JSExpressionImpl {
  public AngularJSAsExpression(ASTNode node) {
    super(node);
  }

  @Nullable
  public JSDefinitionExpression getDefinition() {
    return PsiTreeUtil.getChildOfType(this, JSDefinitionExpression.class);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof AngularJSElementVisitor) {
      ((AngularJSElementVisitor)visitor).visitAngularJSAsExpression(this);
    } else {
      super.accept(visitor);
    }
  }
}
