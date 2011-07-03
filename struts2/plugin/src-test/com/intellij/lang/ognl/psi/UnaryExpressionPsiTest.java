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

package com.intellij.lang.ognl.psi;

import com.intellij.lang.ognl.parsing.OgnlElementTypes;
import com.intellij.psi.PsiType;

/**
 * {@link OgnlUnaryExpression}.
 *
 * @author Yann C&eacute;bron
 */
public class UnaryExpressionPsiTest extends PsiTestCase {

  public void testMinusInteger() {
    final OgnlUnaryExpression expression = parse("-3");
    assertEquals(OgnlTokenTypes.MINUS, expression.getOperation());
    final OgnlExpression operand = expression.getOperand();
    assertElementType(OgnlElementTypes.INTEGER_LITERAL, operand);
    assertEquals(PsiType.INT, expression.getType());
  }

  // not  ====================

  public void testNot() {
    assertConstantUnaryExpression("!true", OgnlTokenTypes.NEGATE, true);
  }

  public void testNotKeyword() {
    assertConstantUnaryExpression("not true", OgnlTokenTypes.NOT_KEYWORD, true);
  }

  public void testBitwiseNot() {
    assertConstantUnaryExpression("~true", OgnlTokenTypes.NOT, true);
  }


  private void assertConstantUnaryExpression(final String expression,
                                             final OgnlTokenType operationSign,
                                             final Object constantValue) {
    final OgnlUnaryExpression unaryExpression = parse(expression);
    assertNotNull(unaryExpression);

    final OgnlTokenType operation = unaryExpression.getOperation();
    assertEquals(operationSign, operation);

    final OgnlExpression operand = unaryExpression.getOperand();
    assertEquals(constantValue, operand.getConstantValue());
  }

  private OgnlUnaryExpression parse(final String expression) {
    return (OgnlUnaryExpression) parseSingleExpression(expression);
  }

}