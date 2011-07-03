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

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * {@link OgnlExpression} with literals.
 *
 * @author Yann C&eacute;bron
 */
public class LiteralExpressionPsiTest extends PsiTestCase {

  public void testStringLiteral() {
    final OgnlExpressionBase expression = parse("\"stringValue\"");
    assertInstanceOf(expression, OgnlStringLiteral.class);
    assertEquals("stringValue", expression.getConstantValue());
  }

  public void testStringLiteralSingleQuotes() {
    final OgnlExpressionBase expression = parse("'stringValue'");
    assertInstanceOf(expression, OgnlStringLiteral.class);
    assertEquals("stringValue", expression.getConstantValue());

    final PsiType type = expression.getType();
    assertNotNull(type);
    assertEquals("java.lang.String", type.getCanonicalText());
  }

  public void testNullLiteral() {
    final OgnlExpressionBase expression = parse("null");
    assertElementType(OgnlElementTypes.NULL_LITERAL, expression);
    assertEquals(PsiType.NULL, expression.getType());
  }

  public void testBooleanLiteral() {
    final OgnlExpressionBase expression = parse("true");
    assertElementType(OgnlElementTypes.BOOLEAN_LITERAL, expression);
    assertEquals(PsiType.BOOLEAN, expression.getType());
    assertEquals(true, expression.getConstantValue());
  }

  public void testIntegerLiteral() {
    final OgnlExpressionBase expression = parse("123");
    assertElementType(OgnlElementTypes.INTEGER_LITERAL, expression);
    assertEquals(PsiType.INT, expression.getType());
    assertEquals(123, expression.getConstantValue());
  }


  public void testBigIntegerLiteralLowerCaseSuffix() {
    runBigIntegerLiteral("123456789h");
  }

  public void testBigIntegerLiteralUpperCaseSuffix() {
    runBigIntegerLiteral("123456789H");
  }

  private void runBigIntegerLiteral(final String bigIntegerExpression) {
    final OgnlExpressionBase expression = parse(bigIntegerExpression);
    assertElementType(OgnlElementTypes.BIG_INTEGER_LITERAL, expression);

    final PsiType type = expression.getType();
    assertNotNull(type);
    assertEquals("java.math.BigInteger", type.getCanonicalText());
    assertEquals(new BigInteger(bigIntegerExpression.substring(0, bigIntegerExpression.length() - 1)),
                 expression.getConstantValue());
  }


  public void testDoubleLiteral() {
    runDoubleLiteral("123.456");
  }

  public void testDoubleLiteralExponent() {
    runDoubleLiteral("123.456e-2");
  }

  public void testDoubleLiteralExponentUpperCase() {
    runDoubleLiteral("123.456E2");
  }

  private void runDoubleLiteral(final String doubleExpression) {
    final OgnlExpressionBase expression = parse(doubleExpression);
    assertElementType(OgnlElementTypes.DOUBLE_LITERAL, expression);
    assertEquals(PsiType.DOUBLE, expression.getType());
    assertEquals(Double.parseDouble(doubleExpression), expression.getConstantValue());
  }


  public void testBigDecimalLiteralLowerCaseSuffix() {
    runBigDecimalLiteral("1234567.89b");
  }

  public void testBigDecimalLiteralUpperCaseSuffix() {
    runBigDecimalLiteral("1234567.89B");
  }

  private void runBigDecimalLiteral(final String bigDecimalExpression) {
    final OgnlExpressionBase expression = parse(bigDecimalExpression);
    assertElementType(OgnlElementTypes.BIG_DECIMAL_LITERAL, expression);

    final PsiType type = expression.getType();
    assertNotNull(type);
    assertEquals("java.math.BigDecimal", type.getCanonicalText());
    assertEquals(new BigDecimal(bigDecimalExpression.substring(0, bigDecimalExpression.length() - 1)),
                 expression.getConstantValue());

  }

  private OgnlExpressionBase parse(final String expression) {
    return (OgnlExpressionBase) parseSingleExpression(expression);
  }

}