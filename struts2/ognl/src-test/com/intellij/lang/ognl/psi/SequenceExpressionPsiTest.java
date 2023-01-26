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
import com.intellij.util.containers.ContainerUtil;
import org.intellij.lang.annotations.Language;

/**
 * {@link OgnlSequenceExpression}.
 *
 * @author Yann C&eacute;bron
 */
public class SequenceExpressionPsiTest extends PsiTestCase {

  public void testSimpleIntegerLiteralSequence() {
    final OgnlSequenceExpression expression = parse("{1,2,3}");
    assertSize(3, expression.getElementsList());

    final OgnlExpression firstExpression = ContainerUtil.getFirstItem(expression.getElementsList());
    assertNotNull(firstExpression);
    assertElementType(OgnlTypes.LITERAL_EXPRESSION, firstExpression);
    assertEquals(PsiTypes.intType(), firstExpression.getType());
  }

  public void testSimpleStringLiteralSequence() {
    final OgnlSequenceExpression expression = parse("{ 'A', \"B\"}");
    assertSize(2, expression.getElementsList());

    final OgnlExpression firstExpression = ContainerUtil.getFirstItem(expression.getElementsList());
    assertNotNull(firstExpression);
    assertElementType(OgnlTypes.LITERAL_EXPRESSION, firstExpression);
    assertEquals(PsiTypes.charType(), firstExpression.getType());
  }

  public void testNestedSimpleIntegerLiteralSequence() {
    final OgnlConditionalExpression expression =
      (OgnlConditionalExpression)parseSingleExpression("a == true ? { 1,2 } : { 2,3 }");
    assertElementType(OgnlTypes.SEQUENCE_EXPRESSION, expression.getThen());
    assertElementType(OgnlTypes.SEQUENCE_EXPRESSION, expression.getElse());
  }

  private OgnlSequenceExpression parse(@Language(value = OgnlLanguage.ID,
                                                 prefix = OgnlLanguage.EXPRESSION_PREFIX,
                                                 suffix = OgnlLanguage.EXPRESSION_SUFFIX) final String expression) {
    return (OgnlSequenceExpression)parseSingleExpression(expression);
  }
}