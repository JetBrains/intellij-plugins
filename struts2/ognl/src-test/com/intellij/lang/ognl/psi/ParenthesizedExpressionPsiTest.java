/*
 * Copyright 2014 The authors
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
 * @author Yann C&eacute;bron
 */
public class ParenthesizedExpressionPsiTest extends PsiTestCase {

  public void testReallySimpleParentheses() {
    final OgnlParenthesizedExpression parenthesizedExpression = parse("(3)");
    final OgnlExpression expression = parenthesizedExpression.getExpression();
    assertElementType(OgnlTypes.LITERAL_EXPRESSION, expression);
  }

  public void testSimpleParentheses() {
    final OgnlParenthesizedExpression parenthesizedExpression = parse("(3 + 4)");
    final OgnlExpression expression = parenthesizedExpression.getExpression();
    assertNotNull(expression);
    assertElementType(OgnlTypes.BINARY_EXPRESSION, expression);
    assertEquals(PsiTypes.intType(), expression.getType());
    assertEquals(PsiTypes.intType(), parenthesizedExpression.getType());

    final OgnlBinaryExpression binaryExpression = (OgnlBinaryExpression)expression;
    assertElementType(OgnlTypes.LITERAL_EXPRESSION, binaryExpression.getLeft());
    assertEquals(OgnlTypes.PLUS, binaryExpression.getOperator());
    assertElementType(OgnlTypes.LITERAL_EXPRESSION, binaryExpression.getRight());
  }

  public void testNestedParentheses() {
    final OgnlParenthesizedExpression parenthesizedExpression = parse("(3 + (4 * 5))");
    final OgnlExpression expression = parenthesizedExpression.getExpression();
    assertNotNull(expression);
    assertElementType(OgnlTypes.BINARY_EXPRESSION, expression);

    final OgnlBinaryExpression binaryExpression = (OgnlBinaryExpression)expression;
    assertElementType(OgnlTypes.LITERAL_EXPRESSION, binaryExpression.getLeft());
    assertEquals(OgnlTypes.PLUS, binaryExpression.getOperator());
    assertElementType(OgnlTypes.PARENTHESIZED_EXPRESSION, binaryExpression.getRight());

    final OgnlParenthesizedExpression nestedParenthesizedExpression = (OgnlParenthesizedExpression)binaryExpression.getRight();
    assertNotNull(nestedParenthesizedExpression);
    assertElementType(OgnlTypes.BINARY_EXPRESSION, nestedParenthesizedExpression.getExpression());
    final OgnlBinaryExpression nestedBinaryExpression = (OgnlBinaryExpression)nestedParenthesizedExpression.getExpression();
    assertNotNull(nestedBinaryExpression);
    assertElementType(OgnlTypes.LITERAL_EXPRESSION, nestedBinaryExpression.getLeft());
    assertEquals(OgnlTypes.MULTIPLY, nestedBinaryExpression.getOperator());
    assertElementType(OgnlTypes.LITERAL_EXPRESSION, nestedBinaryExpression.getRight());
    assertEquals(PsiTypes.intType(), nestedBinaryExpression.getType());
  }

  public void testNestedParenthesesWithMethodCall() {
    final OgnlParenthesizedExpression parenthesizedExpression = parse("(3 + ( multiply(4, 5)))");
    assertEquals("(3 + ( multiply(4, 5)))", parenthesizedExpression.getText());
    final OgnlExpression expression = parenthesizedExpression.getExpression();
    assertElementType(OgnlTypes.BINARY_EXPRESSION, expression);

    final OgnlBinaryExpression binaryExpression = (OgnlBinaryExpression)expression;
    assertNotNull(binaryExpression);
    assertElementType(OgnlTypes.LITERAL_EXPRESSION, binaryExpression.getLeft());
    assertEquals(OgnlTypes.PLUS, binaryExpression.getOperator());
    assertElementType(OgnlTypes.PARENTHESIZED_EXPRESSION, binaryExpression.getRight());

    final OgnlParenthesizedExpression nestedParenthesizedExpression = (OgnlParenthesizedExpression)binaryExpression.getRight();
    assertNotNull(nestedParenthesizedExpression);
    assertElementType(OgnlTypes.METHOD_CALL_EXPRESSION, nestedParenthesizedExpression.getExpression());
  }

  private OgnlParenthesizedExpression parse(@Language(value = OgnlLanguage.ID,
                                                      prefix = OgnlLanguage.EXPRESSION_PREFIX,
                                                      suffix = OgnlLanguage.EXPRESSION_SUFFIX) final String expression) {
    return (OgnlParenthesizedExpression)parseSingleExpression(expression);
  }
}