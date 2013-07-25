/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.coldFusion.model.psi;

import com.intellij.psi.CommonClassNames;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.psi.util.TypeConversionUtil.isNumericType;
import static com.intellij.psi.util.TypeConversionUtil.unboxAndBalanceTypes;

/**
 * Created by IntelliJ IDEA.
 * User: vnikolaenko
 * Date: 28.04.2009
 */
public abstract class CfmlExpressionTypeCalculator {
  private CfmlExpressionTypeCalculator() {
  }

  @Nullable
  public PsiType calculateBinary(@NotNull CfmlExpression leftOperand, @NotNull CfmlExpression rightOperand) {
    return checkAndReturnNumeric(leftOperand, rightOperand);
  }

  @Nullable
  public PsiType calculateUnary(@NotNull CfmlExpression expression) {
    throw new AssertionError(this);
  }

  @Nullable
  private static PsiType checkAndReturnNumeric(@NotNull CfmlExpression leftOperand, @NotNull CfmlExpression rightOperand) {
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
    public PsiType calculateUnary(@NotNull CfmlExpression operand) {
      PsiType type = operand.getPsiType();
      return type != null && isNumericType(type) ? type : null;
    }
  };

  public static final CfmlExpressionTypeCalculator MULTIPLICATIVE_CALCULATOR = new CfmlExpressionTypeCalculator() {
  };

  public static final CfmlExpressionTypeCalculator CONCATINATION_CALCULATOR = new CfmlExpressionTypeCalculator() {
    public PsiType calculateBinary(@NotNull CfmlExpression leftOperand, @NotNull CfmlExpression rightOperand) {
      return CfmlPsiUtil.getTypeByName(CommonClassNames.JAVA_LANG_STRING, leftOperand.getProject());
    }
  };

  public static final CfmlExpressionTypeCalculator BOOLEAN_CALCULATOR = new CfmlExpressionTypeCalculator() {
    public PsiType calculateBinary(@NotNull CfmlExpression leftOperand, @NotNull CfmlExpression rightOperand) {
      return PsiType.BOOLEAN;
    }

    public PsiType calculateUnary(@NotNull CfmlExpression operand) {
      return PsiType.BOOLEAN;
    }
  };
}
