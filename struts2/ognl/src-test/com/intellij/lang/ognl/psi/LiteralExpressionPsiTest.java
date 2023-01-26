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
import com.intellij.psi.CommonClassNames;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypes;
import org.intellij.lang.annotations.Language;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * {@link OgnlExpression} with literals.
 *
 * @author Yann C&eacute;bron
 */
public class LiteralExpressionPsiTest extends PsiTestCase {

  public void testStringLiteral() {
    final OgnlLiteralExpression expression = parse("\"stringValue\"");
    assertEquals(CommonClassNames.JAVA_LANG_STRING, expression.getType().getCanonicalText());
    assertEquals("stringValue", expression.getConstantValue());
  }

  public void testStringLiteralSingleQuotes() {
    final OgnlLiteralExpression expression = parse("'stringValue'");
    assertEquals(PsiTypes.charType(), expression.getType());
    assertEquals("stringValue", expression.getConstantValue());
  }

  public void testNullLiteral() {
    final OgnlLiteralExpression expression = parse("null");
    assertEquals(PsiTypes.nullType(), expression.getType());
    assertEquals(null, expression.getConstantValue());
  }

  public void testBooleanLiteral() {
    final OgnlLiteralExpression expression = parse("true");
    assertEquals(PsiTypes.booleanType(), expression.getType());
    assertEquals(true, expression.getConstantValue());
  }

  public void testIntegerLiteral() {
    final OgnlLiteralExpression expression = parse("123");
    assertEquals(PsiTypes.intType(), expression.getType());
    assertEquals(123, expression.getConstantValue());
  }


  public void testBigIntegerLiteralLowerCaseSuffix() {
    runBigIntegerLiteral("123456789h");
  }

  public void testBigIntegerLiteralUpperCaseSuffix() {
    runBigIntegerLiteral("123456789H");
  }

  private void runBigIntegerLiteral(@Language(value = OgnlLanguage.ID,
                                              prefix = OgnlLanguage.EXPRESSION_PREFIX,
                                              suffix = OgnlLanguage.EXPRESSION_SUFFIX) final String bigIntegerExpression) {
    final OgnlLiteralExpression expression = parse(bigIntegerExpression);

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

  private void runDoubleLiteral(@Language(value = OgnlLanguage.ID,
                                          prefix = OgnlLanguage.EXPRESSION_PREFIX,
                                          suffix = OgnlLanguage.EXPRESSION_SUFFIX) final String doubleExpression) {
    final OgnlLiteralExpression expression = parse(doubleExpression);
    assertEquals(PsiTypes.doubleType(), expression.getType());
    assertEquals(Double.parseDouble(doubleExpression), expression.getConstantValue());
  }


  public void testBigDecimalLiteralLowerCaseSuffix() {
    runBigDecimalLiteral("1234567.89b");
  }

  public void testBigDecimalLiteralUpperCaseSuffix() {
    runBigDecimalLiteral("1234567.89B");
  }

  private void runBigDecimalLiteral(@Language(value = OgnlLanguage.ID,
                                              prefix = OgnlLanguage.EXPRESSION_PREFIX,
                                              suffix = OgnlLanguage.EXPRESSION_SUFFIX) final String bigDecimalExpression) {
    final OgnlLiteralExpression expression = parse(bigDecimalExpression);

    final PsiType type = expression.getType();
    assertNotNull(type);
    assertEquals("java.math.BigDecimal", type.getCanonicalText());
    assertEquals(new BigDecimal(bigDecimalExpression.substring(0, bigDecimalExpression.length() - 1)),
                 expression.getConstantValue());
  }

  private OgnlLiteralExpression parse(@Language(value = OgnlLanguage.ID,
                                                prefix = OgnlLanguage.EXPRESSION_PREFIX,
                                                suffix = OgnlLanguage.EXPRESSION_SUFFIX) final String expression) {
    return (OgnlLiteralExpression)parseSingleExpression(expression);
  }
}