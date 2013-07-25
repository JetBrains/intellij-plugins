package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NotNull;

/**
 * @author vnikolaenko
 * @date 16.02.11
 */
public class CfmlNewExpression extends CfmlCompositeElement implements CfmlTypedElement, CfmlExpression {
  public CfmlNewExpression(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public PsiType getPsiType() {
    CfmlComponentConstructorCall childByClass = findChildByClass(CfmlComponentConstructorCall.class);
    if (childByClass != null) {
      CfmlComponentReference referenceExpression = childByClass.getReferenceExpression();
      if (referenceExpression != null) {
        CfmlFile containingFile = getContainingFile();
        if (containingFile != null) {
          return new CfmlComponentType(referenceExpression.getComponentQualifiedName(referenceExpression.getText()),
                                       containingFile,
                                       getProject());
        }
      }
    }
    return null;
  }

  /*
  @Override
  public PsiReference getReference() {
    CfmlFunctionCallExpression childFunction = findChildByClass(CfmlFunctionCallExpression.class);
    if (childFunction != null) {
      return childFunction.getReferenceExpression();
    }
    return super.getReference();
  }
  */
}
