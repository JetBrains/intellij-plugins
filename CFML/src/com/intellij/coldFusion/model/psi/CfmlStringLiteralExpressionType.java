package com.intellij.coldFusion.model.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.CommonClassNames;
import com.intellij.psi.PsiElement;

/**
 * @author vnikolaenko
 */
public class CfmlStringLiteralExpressionType extends CfmlLiteralExpressionType {
  public CfmlStringLiteralExpressionType() {
    super("StringLiteral", CommonClassNames.JAVA_LANG_STRING);
  }

  @Override
  public PsiElement createPsiElement(ASTNode node) {
    return new CfmlStringLiteralExpression(node);
  }
}
