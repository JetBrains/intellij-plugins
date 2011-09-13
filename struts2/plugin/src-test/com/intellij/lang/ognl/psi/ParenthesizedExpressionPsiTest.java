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

import com.intellij.lang.ognl.OgnlLanguage;
import com.intellij.lang.ognl.parsing.OgnlElementTypes;
import com.intellij.psi.PsiType;
import org.intellij.lang.annotations.Language;

/**
 * @author Yann C&eacute;bron
 */
public class ParenthesizedExpressionPsiTest extends PsiTestCase {

  public void testSimpleParentheses() {
    final OgnlParenthesizedExpression parenthesizedExpression = parse("(3 + 4)");
    final OgnlExpression expression = parenthesizedExpression.getExpression();
    assertElementType(OgnlElementTypes.BINARY_EXPRESSION, expression);
    assertEquals(PsiType.INT, expression.getType());

    final OgnlBinaryExpression binaryExpression = (OgnlBinaryExpression) expression;
    assertElementType(OgnlElementTypes.INTEGER_LITERAL, binaryExpression.getLeftOperand());
    assertEquals(OgnlTokenTypes.PLUS, binaryExpression.getOperationSign());
    assertElementType(OgnlElementTypes.INTEGER_LITERAL, binaryExpression.getRightOperand());
  }

  public void testNestedParentheses() {
    final OgnlParenthesizedExpression parenthesizedExpression = parse("(3 + (4 * 5))");
    final OgnlExpression expression = parenthesizedExpression.getExpression();
    assertElementType(OgnlElementTypes.BINARY_EXPRESSION, expression);

    final OgnlBinaryExpression binaryExpression = (OgnlBinaryExpression) expression;
    assertElementType(OgnlElementTypes.INTEGER_LITERAL, binaryExpression.getLeftOperand());
    assertEquals(OgnlTokenTypes.PLUS, binaryExpression.getOperationSign());
    assertElementType(OgnlElementTypes.PARENTHESIZED_EXPRESSION, binaryExpression.getRightOperand());

    final OgnlParenthesizedExpression nestedParenthesizedExpression = (OgnlParenthesizedExpression) binaryExpression.getRightOperand();
    assertNotNull(nestedParenthesizedExpression);
    assertElementType(OgnlElementTypes.BINARY_EXPRESSION, nestedParenthesizedExpression.getExpression());
    final OgnlBinaryExpression nestedBinaryExpression = (OgnlBinaryExpression) nestedParenthesizedExpression.getExpression();
    assertElementType(OgnlElementTypes.INTEGER_LITERAL, nestedBinaryExpression.getLeftOperand());
    assertEquals(OgnlTokenTypes.MULTIPLY, nestedBinaryExpression.getOperationSign());
    assertElementType(OgnlElementTypes.INTEGER_LITERAL, nestedBinaryExpression.getRightOperand());
  }

  public void testNestedParenthesesWithMethodCall() {
    final OgnlParenthesizedExpression parenthesizedExpression = parse("(3 + ( multiply(4, 5)))");
    assertEquals("(3 + ( multiply(4, 5)))", parenthesizedExpression.getText());
    final OgnlExpression expression = parenthesizedExpression.getExpression();
    assertElementType(OgnlElementTypes.BINARY_EXPRESSION, expression);

    final OgnlBinaryExpression binaryExpression = (OgnlBinaryExpression) expression;
    assertElementType(OgnlElementTypes.INTEGER_LITERAL, binaryExpression.getLeftOperand());
    assertEquals(OgnlTokenTypes.PLUS, binaryExpression.getOperationSign());
    assertElementType(OgnlElementTypes.PARENTHESIZED_EXPRESSION, binaryExpression.getRightOperand());

    final OgnlParenthesizedExpression nestedParenthesizedExpression = (OgnlParenthesizedExpression) binaryExpression.getRightOperand();
    assertNotNull(nestedParenthesizedExpression);
    assertElementType(OgnlElementTypes.METHOD_CALL_EXPRESSION, nestedParenthesizedExpression.getExpression());
  }

  private OgnlParenthesizedExpression parse(@Language(value = OgnlLanguage.ID,
                                                      prefix = OgnlLanguage.EXPRESSION_PREFIX,
                                                      suffix = OgnlLanguage.EXPRESSION_SUFFIX) final String expression) {
    return (OgnlParenthesizedExpression) parseSingleExpression(expression);
  }

}