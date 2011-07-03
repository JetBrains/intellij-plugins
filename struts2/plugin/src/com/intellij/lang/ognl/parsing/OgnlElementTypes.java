/*
 * Copyright 2011 The authors
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

package com.intellij.lang.ognl.parsing;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ognl.psi.*;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Yann C&eacute;bron
 */
public class OgnlElementTypes {

  // %{ ... }
  public static final OgnlElementType EXPRESSION_HOLDER = new OgnlElementType("EXPRESSION_HOLDER");

  public static final OgnlElementType PARENTHESIZED_EXPRESSION = new OgnlElementType("ParenthesizedExpression");
  public static final OgnlElementType INDEXED_EXPRESSION = new OgnlElementType("IndexedExpression");

  public static final OgnlElementType BINARY_EXPRESSION = new OgnlElementType("BinaryExpression") {
    @Override
    public PsiElement createPsiElement(final ASTNode node) {
      return new OgnlBinaryExpression(node);
    }
  };

  public static final OgnlElementType CONDITIONAL_EXPRESSION = new OgnlElementType("ConditionalExpression") {
    @Override
    public PsiElement createPsiElement(final ASTNode node) {
      return new OgnlConditionalExpression(node);
    }
  };

  public static final OgnlElementType REFERENCE_EXPRESSION = new OgnlElementType("ReferenceExpression") {
    @Override
    public PsiElement createPsiElement(final ASTNode node) {
      return new OgnlReferenceExpression(node);
    }
  };

  public static final OgnlElementType VARIABLE_EXPRESSION = new OgnlElementType("VariableExpression") {
    @Override
    public PsiElement createPsiElement(final ASTNode node) {
      return new OgnlVariableExpression(node);
    }
  };

  public static final OgnlElementType UNARY_EXPRESSION = new OgnlElementType("UnaryExpression") {
    @Override
    public PsiElement createPsiElement(final ASTNode node) {
      return new OgnlUnaryExpression(node);
    }
  };

  public static final OgnlElementType STRING_LITERAL = new OgnlElementType("StringLiteral") {
    public PsiElement createPsiElement(final ASTNode node) {
      return new OgnlStringLiteral(node);
    }
  };

  public static final OgnlElementType NULL_LITERAL = new OgnlElementType("NullLiteral") {
    @Override
    public PsiElement createPsiElement(final ASTNode node) {
      return new OgnlExpressionBase(node) {
        @Override
        public PsiType getType() {
          return PsiType.NULL;
        }

      };
    }
  };

  public static final OgnlElementType BOOLEAN_LITERAL = new OgnlElementType("BooleanLiteral") {
    public PsiElement createPsiElement(final ASTNode node) {
      return new OgnlExpressionBase(node) {

        @Override
        public PsiType getType() {
          return PsiType.BOOLEAN;
        }

        @Override
        public Object getConstantValue() {
          return Boolean.valueOf(getText());
        }
      };
    }
  };

  public static final OgnlElementType INTEGER_LITERAL = new OgnlElementType("IntegerLiteral") {
    @Override
    public PsiElement createPsiElement(final ASTNode node) {
      return new OgnlExpressionBase(node) {

        @Override
        public PsiType getType() {
          return PsiType.INT;
        }

        @Override
        public Object getConstantValue() {
          return Integer.parseInt(getText());
        }
      };
    }
  };

  public static final OgnlElementType BIG_INTEGER_LITERAL = new OgnlElementType("BigIntegerLiteral") {
    @Override
    public PsiElement createPsiElement(final ASTNode node) {
      return new OgnlExpressionBase(node) {
        @Override
        public PsiType getType() {
          return JavaPsiFacade.getInstance(getProject()).getElementFactory()
                              .createTypeByFQClassName("java.math.BigInteger", getResolveScope());
        }

        @Override
        public Object getConstantValue() {
          return new BigInteger(getText().substring(0, getTextLength() - 1));
        }
      };
    }
  };

  public static final OgnlElementType DOUBLE_LITERAL = new OgnlElementType("DoubleLiteral") {
    @Override
    public PsiElement createPsiElement(final ASTNode node) {
      return new OgnlExpressionBase(node) {

        @Override
        public PsiType getType() {
          return PsiType.DOUBLE;
        }

        @Override
        public Object getConstantValue() {
          return Double.parseDouble(getText());
        }
      };
    }
  };

  public static final OgnlElementType BIG_DECIMAL_LITERAL = new OgnlElementType("BigDecimalLiteral") {
    @Override
    public PsiElement createPsiElement(final ASTNode node) {
      return new OgnlExpressionBase(node) {
        @Override
        public PsiType getType() {
          return JavaPsiFacade.getInstance(getProject()).getElementFactory()
                              .createTypeByFQClassName("java.math.BigDecimal", getResolveScope());
        }

        @Override
        public Object getConstantValue() {
          return new BigDecimal(getText().substring(0, getTextLength() - 1));
        }
      };
    }
  };

}