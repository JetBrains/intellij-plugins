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
import org.intellij.lang.annotations.Language;

/**
 * @author Yann C&eacute;bron
 */
public class MethodCallExpressionPsiTest extends PsiTestCase {

  public void testMethodCallNoParams() {
    final OgnlMethodCallExpression methodCallExpression = parse("methodName()");
    assertEquals(0, methodCallExpression.getParameterCount());
  }

  public void testMethodCallOneParameter() {
    final OgnlMethodCallExpression methodCallExpression = parse("methodName(1)");
    final OgnlExpression method = methodCallExpression.getMethod();
    assertElementType(OgnlElementTypes.REFERENCE_EXPRESSION, method);
    assertEquals("methodName", method.getText());
    assertEquals(1, methodCallExpression.getParameterCount());
    final OgnlExpression parameter = methodCallExpression.getParameter(0);
    assertElementType(OgnlElementTypes.INTEGER_LITERAL, parameter);
  }

  public void testMethodCallTwoParameters() {
    final OgnlMethodCallExpression methodCallExpression = parse("methodName(1, 'someText')");
    assertEquals(2, methodCallExpression.getParameterCount());
    final OgnlExpression parameter = methodCallExpression.getParameter(1);
    assertElementType(OgnlElementTypes.STRING_LITERAL, parameter);
  }

  private OgnlMethodCallExpression parse(@Language(value = OgnlLanguage.ID,
                                                   prefix = OgnlLanguage.EXPRESSION_PREFIX,
                                                   suffix = OgnlLanguage.EXPRESSION_SUFFIX) final String expression) {
    return (OgnlMethodCallExpression) parseSingleExpression(expression);
  }

}