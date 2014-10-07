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
import org.intellij.lang.annotations.Language;

/**
 * {@link com.intellij.lang.ognl.psi.OgnlProjectionExpression}.
 *
 * @author Yann C&eacute;bron
 */
public class ProjectionExpressionPsiTest extends PsiTestCase {

  public void testThisExpression() {
    final OgnlReferenceExpression expression = parse("myList.{#this}");

    final OgnlExpression nestedExpression = assertOneElement(expression.getExpressionList());
    final OgnlProjectionExpression projectionExpression = assertInstanceOf(nestedExpression, OgnlProjectionExpression.class);
    assertEquals("{#this}", projectionExpression.getText());

    assertElementType(OgnlTypes.VARIABLE_EXPRESSION, projectionExpression.getProjectionExpression());
  }

  private OgnlReferenceExpression parse(@Language(value = OgnlLanguage.ID,
    prefix = OgnlLanguage.EXPRESSION_PREFIX,
    suffix = OgnlLanguage.EXPRESSION_SUFFIX) final String expression) {
    return (OgnlReferenceExpression)parseSingleExpression(expression);
  }
}