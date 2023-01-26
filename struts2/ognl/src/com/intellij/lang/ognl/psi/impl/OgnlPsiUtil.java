/*
 * Copyright 2018 The authors
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

package com.intellij.lang.ognl.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ognl.OgnlTypes;
import com.intellij.lang.ognl.psi.*;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceProvider;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;

/**
 * @author Yann C&eacute;bron
 */
final class OgnlPsiUtil {

  @NotNull
  static OgnlTokenType getOperator(OgnlBinaryExpression expression) {
    final ASTNode node = expression.getNode().findChildByType(OgnlTokenGroups.OPERATIONS);
    assert node != null : "unknown operation sign: '" + expression.getText() + "'";
    return (OgnlTokenType)node.getElementType();
  }

  @NotNull
  static OgnlTokenType getUnaryOperator(OgnlUnaryExpression expression) {
    final ASTNode node = expression.getNode().findChildByType(OgnlTokenGroups.UNARY_OPS);
    assert node != null : "unknown unary operation sign: '" + expression.getText() + "'";
    return (OgnlTokenType)node.getElementType();
  }

  @NotNull
  static String getVariableName(OgnlVariableAssignmentExpression expression) {
    final ASTNode nameNode = expression.getNode().findChildByType(OgnlTypes.IDENTIFIER);
    assert nameNode != null;
    return nameNode.getText();
  }

  @Nullable
  static PsiType getType(@Nullable OgnlExpression expression) {
    if (expression == null) {
      return null;
    }

    if (expression instanceof OgnlLiteralExpression) {
      return resolveLiteralExpressionType((OgnlLiteralExpression)expression);
    }

    if (expression instanceof OgnlConditionalExpression) {
      final OgnlExpression thenExpression = ((OgnlConditionalExpression)expression).getThen();
      return getType(thenExpression);
    }

    if (expression instanceof OgnlBinaryExpression) {
      OgnlExpression leftExpression = ((OgnlBinaryExpression)expression).getLeft();
      return getType(leftExpression);
    }

    if (expression instanceof OgnlVariableAssignmentExpression) {
      OgnlExpression assignment = ((OgnlVariableAssignmentExpression)expression).getAssignment();
      return getType(assignment);
    }

    if (expression instanceof OgnlParenthesizedExpression) {
      OgnlExpression argument = ((OgnlParenthesizedExpression)expression).getExpression();
      return getType(argument);
    }

    return null;
  }

  private static PsiType resolveLiteralExpressionType(OgnlLiteralExpression expression) {
    final ASTNode node = expression.getNode();
    final IElementType type = node.getFirstChildNode().getElementType();

    if (type == OgnlTypes.STRING_LITERAL) {
      return PsiType.getJavaLangString(expression.getManager(), expression.getResolveScope());
    }
    if (type == OgnlTypes.CHARACTER_LITERAL) {
      return PsiTypes.charType();
    }
    if (type == OgnlTypes.INTEGER_LITERAL) {
      return PsiTypes.intType();
    }
    if (type == OgnlTypes.BIG_INTEGER_LITERAL) {
      return JavaPsiFacade.getInstance(expression.getProject()).getElementFactory()
                          .createTypeByFQClassName("java.math.BigInteger", expression.getResolveScope());
    }
    if (type == OgnlTypes.DOUBLE_LITERAL) {
      return PsiTypes.doubleType();
    }
    if (type == OgnlTypes.BIG_DECIMAL_LITERAL) {
      return JavaPsiFacade.getInstance(expression.getProject()).getElementFactory()
                          .createTypeByFQClassName("java.math.BigDecimal", expression.getResolveScope());
    }
    if (type == OgnlTypes.TRUE_KEYWORD ||
        type == OgnlTypes.FALSE_KEYWORD) {
      return PsiTypes.booleanType();
    }
    if (type == OgnlTypes.NULL_KEYWORD) {
      return PsiTypes.nullType();
    }

    throw new IllegalArgumentException("could not resolve type for literal " + type + " / " + expression.getText());
  }

  @Nullable
  static Object getConstantValue(OgnlLiteralExpression expression) {
    final ASTNode node = expression.getNode();
    final IElementType type = node.getFirstChildNode().getElementType();

    final String text = expression.getText();
    if (type == OgnlTypes.STRING_LITERAL) {
      return StringUtil.unquoteString(text);
    }
    if (type == OgnlTypes.CHARACTER_LITERAL) {
      return StringUtil.unquoteString(text);
    }
    if (type == OgnlTypes.INTEGER_LITERAL) {
      return Integer.parseInt(text);
    }
    if (type == OgnlTypes.BIG_INTEGER_LITERAL) {
      return new BigInteger(text.substring(0, expression.getTextLength() - 1));
    }
    if (type == OgnlTypes.DOUBLE_LITERAL) {
      return Double.parseDouble(text);
    }
    if (type == OgnlTypes.BIG_DECIMAL_LITERAL) {
      return new BigDecimal(text.substring(0, expression.getTextLength() - 1));
    }
    if (type == OgnlTypes.TRUE_KEYWORD ||
        type == OgnlTypes.FALSE_KEYWORD) {
      return Boolean.valueOf(text);
    }
    if (type == OgnlTypes.NULL_KEYWORD) {
      return null;
    }

    throw new IllegalArgumentException("could not resolve constant value for literal " + type + " / " + text);
  }

  static int getParameterCount(OgnlParameterList parameterList) {
    return parameterList.getParametersList().size();
  }

  static void customizeFqnTypeExpressionReferences(OgnlFqnTypeExpression fqnTypeExpression,
                                                   JavaClassReferenceProvider referenceProvider) {
    PsiElement parent = fqnTypeExpression.getParent();
    if (parent instanceof OgnlNewExpression ||
        parent instanceof OgnlNewArrayExpression) {
      referenceProvider.setOption(JavaClassReferenceProvider.CONCRETE, Boolean.TRUE);
      referenceProvider.setOption(JavaClassReferenceProvider.NOT_INTERFACE, Boolean.TRUE);
      referenceProvider.setOption(JavaClassReferenceProvider.NOT_ENUM, Boolean.TRUE);
      return;
    }

    if (parent instanceof OgnlMapExpression) {
      referenceProvider.setOption(JavaClassReferenceProvider.CONCRETE, Boolean.TRUE);
      referenceProvider.setOption(JavaClassReferenceProvider.INSTANTIATABLE, Boolean.TRUE);
      referenceProvider.setOption(JavaClassReferenceProvider.SUPER_CLASSES,
                                  Collections.singletonList(CommonClassNames.JAVA_UTIL_MAP));
    }
  }
}
