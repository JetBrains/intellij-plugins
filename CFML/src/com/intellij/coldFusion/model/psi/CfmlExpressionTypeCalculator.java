// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.psi;

import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.psi.util.TypeConversionUtil.isNumericType;
import static com.intellij.psi.util.TypeConversionUtil.unboxAndBalanceTypes;

public abstract class CfmlExpressionTypeCalculator {
  private CfmlExpressionTypeCalculator() {
  }

  public @Nullable PsiType calculateBinary(@NotNull CfmlExpression leftOperand, @NotNull CfmlExpression rightOperand) {
    return checkAndReturnNumeric(leftOperand, rightOperand);
  }

  public @Nullable PsiType calculateUnary(@NotNull CfmlExpression expression) {
    throw new AssertionError(this);
  }

  private static @Nullable PsiType checkAndReturnNumeric(@NotNull CfmlExpression leftOperand, @NotNull CfmlExpression rightOperand) {
    PsiType rightType = rightOperand.getPsiType();
    if (rightType == null) {
      return null;
    }
    PsiType leftType = leftOperand.getPsiType();
    if (leftType == null) {
      return null;
    }
    if (isNumericType(leftType) && isNumericType(rightType)) {
      PsiClassType boxedType =
        ((PsiPrimitiveType)unboxAndBalanceTypes(leftType, rightType)).getBoxedType(leftOperand.getManager(), leftOperand.getResolveScope());
      return boxedType;
    }
    return null;
  }

  public static final CfmlExpressionTypeCalculator PLUS_CALCULATOR = new CfmlExpressionTypeCalculator() {
    @Override
    public PsiType calculateBinary(@NotNull CfmlExpression leftOperand, @NotNull CfmlExpression rightOperand) {
      // TODO: more smart (get rid of exception from code below)
      PsiType rightType = rightOperand.getPsiType();
      if (rightType == null || rightType.equalsToText(CommonClassNames.JAVA_LANG_STRING)) {
        return rightType;
      }
      PsiType leftType = leftOperand.getPsiType();
      if (leftType == null || leftType.equalsToText(CommonClassNames.JAVA_LANG_STRING)) {
        return leftType;
      }
      return checkAndReturnNumeric(leftOperand, rightOperand);
    }
  };

  public static final CfmlExpressionTypeCalculator MINUS_CALCULATOR = new CfmlExpressionTypeCalculator() {
    @Override
    public PsiType calculateUnary(@NotNull CfmlExpression operand) {
      PsiType type = operand.getPsiType();
      return type != null && isNumericType(type) ? type : null;
    }
  };

  public static final CfmlExpressionTypeCalculator MULTIPLICATIVE_CALCULATOR = new CfmlExpressionTypeCalculator() {
  };

  public static final CfmlExpressionTypeCalculator CONCATINATION_CALCULATOR = new CfmlExpressionTypeCalculator() {
    @Override
    public PsiType calculateBinary(@NotNull CfmlExpression leftOperand, @NotNull CfmlExpression rightOperand) {
      return CfmlPsiUtil.getTypeByName(CommonClassNames.JAVA_LANG_STRING, leftOperand.getProject());
    }
  };

  public static final CfmlExpressionTypeCalculator BOOLEAN_CALCULATOR = new CfmlExpressionTypeCalculator() {
    @Override
    public PsiType calculateBinary(@NotNull CfmlExpression leftOperand, @NotNull CfmlExpression rightOperand) {
      return PsiTypes.booleanType();
    }

    @Override
    public PsiType calculateUnary(@NotNull CfmlExpression operand) {
      return PsiTypes.booleanType();
    }
  };
}
