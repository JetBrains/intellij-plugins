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
import com.intellij.psi.PsiTypes;
import org.intellij.lang.annotations.Language;

/**
 * {@link OgnlConditionalExpression}.
 *
 * @author Yann C&eacute;bron
 */
public class ConditionalExpressionPsiTest extends PsiTestCase {

  public void testSimple() {
    final OgnlConditionalExpression expression = parse("true ? 0 : 1");
    assertEquals(PsiTypes.intType(), expression.getType());

    final OgnlExpression condition = expression.getCondition();
    assertEquals("true", condition.getText());
    assertElementType(OgnlTypes.LITERAL_EXPRESSION, condition);
    assertEquals(PsiTypes.booleanType(), condition.getType());

    final OgnlExpression thenExpression = expression.getThen();
    assertNotNull(thenExpression);
    assertEquals("0", thenExpression.getText());
    assertElementType(OgnlTypes.LITERAL_EXPRESSION, thenExpression);
    final OgnlLiteralExpression thenLiteral = assertInstanceOf(thenExpression, OgnlLiteralExpression.class);
    assertEquals(PsiTypes.intType(), thenLiteral.getType());
    assertEquals(0, thenLiteral.getConstantValue());

    final OgnlExpression elseExpression = expression.getElse();
    assertNotNull(elseExpression);
    assertEquals("1", elseExpression.getText());
    assertElementType(OgnlTypes.LITERAL_EXPRESSION, elseExpression);
    final OgnlLiteralExpression elseLiteral = assertInstanceOf(elseExpression, OgnlLiteralExpression.class);
    assertEquals(PsiTypes.intType(), elseLiteral.getType());
    assertEquals(1, elseLiteral.getConstantValue());
  }

  public void testConditionalWithVar() {
    parse("#this > 100 ? 2 * this : 20 + #this");
  }

  public void testVariableExpressionInThen() {
    final OgnlConditionalExpression expression = parse("true ? #this : 1");
    final OgnlExpression thenExpression = expression.getThen();
    assertElementType(OgnlTypes.VARIABLE_EXPRESSION, thenExpression);
  }

  public void testBinaryExpressionInThen() {
    final OgnlConditionalExpression expression = parse("true ? 1 + 2 : 1");
    final OgnlExpression thenExpression = expression.getThen();
    assertElementType(OgnlTypes.BINARY_EXPRESSION, thenExpression);
  }

  public void testParenthesizedExpressionInThen() {
    final OgnlConditionalExpression expression = parse("true ? (1 + 2) : 1");
    final OgnlExpression thenExpression = expression.getThen();
    assertElementType(OgnlTypes.PARENTHESIZED_EXPRESSION, thenExpression);
  }

  private OgnlConditionalExpression parse(@Language(value = OgnlLanguage.ID,
                                                    prefix = OgnlLanguage.EXPRESSION_PREFIX,
                                                    suffix = OgnlLanguage.EXPRESSION_SUFFIX) final String expression) {
    return (OgnlConditionalExpression)parseSingleExpression(expression);
  }
}