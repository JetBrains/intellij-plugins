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

package com.intellij.lang.ognl.psi;

import com.intellij.lang.ognl.OgnlLanguage;
import com.intellij.lang.ognl.OgnlTypes;
import com.intellij.psi.PsiType;
import com.intellij.psi.tree.IElementType;
import org.intellij.lang.annotations.Language;

/**
 * {@link OgnlUnaryExpression}.
 *
 * @author Yann C&eacute;bron
 */
public class UnaryExpressionPsiTest extends PsiTestCase {

  public void testMinusInteger() {
    final OgnlUnaryExpression expression = parse("-3");
    assertEquals(OgnlTypes.MINUS, expression.getUnaryOperator());

    final OgnlExpression operand = expression.getExpression();
    assertElementType(OgnlTypes.LITERAL_EXPRESSION, operand);
    final OgnlLiteralExpression literalExpression = assertInstanceOf(operand, OgnlLiteralExpression.class);
    assertEquals(PsiType.INT, literalExpression.getType());
  }

  // not  ====================

  public void testNot() {
    assertConstantUnaryExpression("!true", OgnlTypes.NEGATE, true);
  }

  public void testNotKeyword() {
    assertConstantUnaryExpression("not true", OgnlTypes.NOT_KEYWORD, true);
  }

  public void testBitwiseNot() {
    assertConstantUnaryExpression("~true", OgnlTypes.NOT, true);
  }


  private void assertConstantUnaryExpression(@Language(value = OgnlLanguage.ID,
                                                       prefix = OgnlLanguage.EXPRESSION_PREFIX,
                                                       suffix = OgnlLanguage.EXPRESSION_SUFFIX) final String expression,
                                             final IElementType operationSign,
                                             final Object constantValue) {
    final OgnlUnaryExpression unaryExpression = parse(expression);
    assertNotNull(unaryExpression);

    final OgnlTokenType operation = unaryExpression.getUnaryOperator();
    assertEquals(operationSign, operation);

    final OgnlExpression operand = unaryExpression.getExpression();
    if (operand instanceof OgnlLiteralExpression) {
      assertEquals(constantValue, ((OgnlLiteralExpression)operand).getConstantValue());
    }
  }

  private OgnlUnaryExpression parse(@Language(value = OgnlLanguage.ID,
                                              prefix = OgnlLanguage.EXPRESSION_PREFIX,
                                              suffix = OgnlLanguage.EXPRESSION_SUFFIX) final String expression) {
    return (OgnlUnaryExpression)parseSingleExpression(expression);
  }
}