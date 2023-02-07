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
import com.intellij.psi.PsiTypes;
import com.intellij.psi.tree.IElementType;
import org.intellij.lang.annotations.Language;

/**
 * {@link OgnlUnaryExpression}.
 *
 * @author Yann C&eacute;bron
 */
public class UnaryExpressionPsiTest extends PsiTestCase {

  public void testMinusInteger() {
    assertConstantUnaryExpression("-3", OgnlTypes.MINUS, PsiTypes.intType(), 3);
  }

  public void testPlusInteger() {
    assertConstantUnaryExpression("+3", OgnlTypes.PLUS, PsiTypes.intType(), 3);
  }

  public void testNegate() {
    assertConstantUnaryExpression("!true", OgnlTypes.NEGATE, PsiTypes.booleanType(), true);
  }


  // not  ====================
  public void testNotKeyword() {
    assertConstantUnaryExpression("not true", OgnlTypes.NOT_KEYWORD, PsiTypes.booleanType(), true);
  }

  public void testBitwiseNot() {
    assertConstantUnaryExpression("~true", OgnlTypes.NOT, PsiTypes.booleanType(), true);
  }


  private void assertConstantUnaryExpression(@Language(value = OgnlLanguage.ID,
                                                       prefix = OgnlLanguage.EXPRESSION_PREFIX,
                                                       suffix = OgnlLanguage.EXPRESSION_SUFFIX) final String expression,
                                             final IElementType operationSign,
                                             final PsiType operandType,
                                             final Object operandConstantValue) {
    final OgnlUnaryExpression unaryExpression = parse(expression);
    assertNotNull(unaryExpression);

    final OgnlTokenType operation = unaryExpression.getUnaryOperator();
    assertEquals(operationSign, operation);

    final OgnlExpression operand = unaryExpression.getExpression();
    assertNotNull(operand);
    assertEquals(operandType, operand.getType());

    if (operand instanceof OgnlLiteralExpression literalExpression) {
      assertEquals(operandConstantValue, literalExpression.getConstantValue());
    }
  }

  private OgnlUnaryExpression parse(@Language(value = OgnlLanguage.ID,
                                              prefix = OgnlLanguage.EXPRESSION_PREFIX,
                                              suffix = OgnlLanguage.EXPRESSION_SUFFIX) final String expression) {
    return (OgnlUnaryExpression)parseSingleExpression(expression);
  }
}