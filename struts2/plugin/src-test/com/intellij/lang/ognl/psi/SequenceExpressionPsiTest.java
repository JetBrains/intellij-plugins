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

/**
 * {@link OgnlSequenceExpression}.
 *
 * @author Yann C&eacute;bron
 */
public class SequenceExpressionPsiTest extends PsiTestCase {

  public void testSimpleIntegerLiteralSequence() {
    final OgnlSequenceExpression expression = parse("{1,2,3}");
    assertSize(3, expression.getElements());
    final OgnlExpression firstExpression = expression.getExpression(0);
    assertElementType(OgnlElementTypes.INTEGER_LITERAL, firstExpression);
  }

  public void testSimpleStringLiteralSequence() {
    final OgnlSequenceExpression expression = parse("{ 'A', \"B\"}");
    assertSize(2, expression.getElements());
    final OgnlExpression firstExpression = expression.getExpression(0);
    assertElementType(OgnlElementTypes.STRING_LITERAL, firstExpression);
  }

  public void testNestedSimpleIntegerLiteralSequence() {
    final OgnlConditionalExpression expression =
        (OgnlConditionalExpression) parseSingleExpression("a == true ? { 1,2 } : { 2,3 }");
    assertElementType(OgnlElementTypes.SEQUENCE_EXPRESSION, (OgnlExpression) expression.getThen());
    assertElementType(OgnlElementTypes.SEQUENCE_EXPRESSION, (OgnlExpression) expression.getThen());
  }

  private OgnlSequenceExpression parse(final String expression) {
    return (OgnlSequenceExpression) parseSingleExpression(expression);
  }

}