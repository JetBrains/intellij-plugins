// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.lexer.CfscriptTokenTypes;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public class CfmlOperatorExpression extends CfmlCompositeElement implements CfmlExpression {

  private final boolean myBinary;

  public CfmlOperatorExpression(final @NotNull ASTNode node, boolean binary) {
    super(node);
    myBinary = binary;
  }

  @Override
  public PsiType getPsiType() {
    CfmlExpressionTypeCalculator typeCalculator = getOperationSign().getTypeCalculator();
    CfmlExpression operand1 = getOperand1();
    if (operand1 == null) {
      return null;
    }
    if (!myBinary) {
      return typeCalculator.calculateUnary(operand1);
    }
    CfmlExpression operand2 = getOperand2();
    if (operand2 == null) {
      return null;
    }
    return typeCalculator.calculateBinary(operand1, operand2);
  }

  private @NotNull CfmlOperatorTokenType getOperationSign() {
    final ASTNode operationNode = getNode().findChildByType(CfscriptTokenTypes.OPERATIONS);
    assert operationNode != null : getText();
    IElementType tokenType = operationNode.getElementType();
    assert tokenType instanceof CfmlOperatorTokenType : getText();
    return (CfmlOperatorTokenType)tokenType;
  }

  private CfmlExpression getOperand1() {
    return findChildByClass(CfmlExpression.class);
  }

  private CfmlExpression getOperand2() {
    CfmlExpression first = getOperand1();
    if (first == null) {
      return null;
    }
    PsiElement second = first.getNextSibling();
    while (second != null && !(second instanceof CfmlExpression)) {
      second = second.getNextSibling();
    }
    return (CfmlExpression)second;
  }
}
