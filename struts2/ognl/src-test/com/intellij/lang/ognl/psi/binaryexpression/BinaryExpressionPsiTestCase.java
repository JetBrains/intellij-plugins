/*
 * Copyright 2013 The authors
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

package com.intellij.lang.ognl.psi.binaryexpression;

import com.intellij.lang.ognl.OgnlLanguage;
import com.intellij.lang.ognl.psi.OgnlBinaryExpression;
import com.intellij.lang.ognl.psi.OgnlExpression;
import com.intellij.lang.ognl.psi.OgnlLiteralExpression;
import com.intellij.lang.ognl.psi.PsiTestCase;
import com.intellij.psi.tree.IElementType;
import org.intellij.lang.annotations.Language;

abstract class BinaryExpressionPsiTestCase extends PsiTestCase {

  protected OgnlBinaryExpression parse(@Language(value = OgnlLanguage.ID,
                                                 prefix = OgnlLanguage.EXPRESSION_PREFIX,
                                                 suffix = OgnlLanguage.EXPRESSION_SUFFIX) final String expression) {
    return (OgnlBinaryExpression)parseSingleExpression(expression);
  }

  protected void assertBinaryExpression(@Language(value = OgnlLanguage.ID,
                                                  prefix = OgnlLanguage.EXPRESSION_PREFIX,
                                                  suffix = OgnlLanguage.EXPRESSION_SUFFIX) final String expression,
                                        final IElementType leftType,
                                        final IElementType operationSign,
                                        final IElementType rightType) {
    final OgnlBinaryExpression binaryExpression = parse(expression);
    assertNotNull(binaryExpression);

    final OgnlExpression leftOperand = binaryExpression.getLeft();
    assertNotNull(leftOperand);
    assertElementType(leftType, leftOperand);

    assertEquals(operationSign, binaryExpression.getOperator());

    final OgnlExpression rightOperand = binaryExpression.getRight();
    assertNotNull(rightOperand);
    assertElementType(rightType, rightOperand);
  }

  protected void assertConstantBinaryExpression(@Language(value = OgnlLanguage.ID,
                                                          prefix = OgnlLanguage.EXPRESSION_PREFIX,
                                                          suffix = OgnlLanguage.EXPRESSION_SUFFIX) final String expression,
                                                final Object leftConstantValue,
                                                final IElementType operationSign,
                                                final Object rightConstantValue) {
    final OgnlBinaryExpression binaryExpression = parse(expression);
    assertNotNull(binaryExpression);

    final OgnlExpression leftOperand = binaryExpression.getLeft();
    assertNotNull(leftOperand);
    if (leftOperand instanceof OgnlLiteralExpression) {
      assertEquals(leftConstantValue, ((OgnlLiteralExpression)leftOperand).getConstantValue());
    }

    assertEquals(operationSign, binaryExpression.getOperator());

    final OgnlExpression rightOperand = binaryExpression.getRight();
    assertNotNull(rightOperand);
    if (rightOperand instanceof OgnlLiteralExpression) {
      assertEquals(rightConstantValue, ((OgnlLiteralExpression)rightOperand).getConstantValue());
    }
  }
}