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
import org.intellij.lang.annotations.Language;

/**
 * @author Yann C&eacute;bron
 */
public class MethodCallExpressionPsiTest extends PsiTestCase {

  public void testMethodCallNoParams() {
    final OgnlMethodCallExpression methodCallExpression = parse("methodName()");
    final OgnlParameterList parameterList = methodCallExpression.getParameterList();
    assertNotNull(parameterList);
    assertEmpty(parameterList.getParametersList());
  }

  public void testMethodCallOneParameter() {
    final OgnlMethodCallExpression methodCallExpression = parse("methodName(1)");
    final OgnlExpression method = methodCallExpression.getMethod();
    assertElementType(OgnlTypes.REFERENCE_EXPRESSION, method);
    assertEquals("methodName", method.getText());

    OgnlParameterList parameterList = methodCallExpression.getParameterList();
    assertNotNull(parameterList);
    assertEquals(1, parameterList.getParameterCount());
    final OgnlExpression parameter = assertOneElement(parameterList.getParametersList());
    assertElementType(OgnlTypes.LITERAL_EXPRESSION, parameter);
  }

  public void testMethodCallTwoParameters() {
    final OgnlMethodCallExpression methodCallExpression = parse("methodName(1, 'someText')");

    OgnlParameterList parameterList = methodCallExpression.getParameterList();
    assertNotNull(parameterList);
    assertEquals(2, parameterList.getParameterCount());
    final OgnlExpression parameter = parameterList.getParametersList().get(1);
    assertElementType(OgnlTypes.LITERAL_EXPRESSION, parameter);
  }

  public void testNestedMethodCalls() {
    final OgnlMethodCallExpression methodCallExpression = parse("method(ensureLoaded(1,2), name)");

    OgnlParameterList parameterList = methodCallExpression.getParameterList();
    assertNotNull(parameterList);
    assertEquals(2, parameterList.getParameterCount());

    final OgnlExpression ensureLoaded = parameterList.getParametersList().get(0);
    assertElementType(OgnlTypes.METHOD_CALL_EXPRESSION, ensureLoaded);

    final OgnlParameterList ensureLoadedParameterList = ((OgnlMethodCallExpression)ensureLoaded).getParameterList();
    assertNotNull(ensureLoadedParameterList);
    assertEquals(2, ensureLoadedParameterList.getParameterCount());

    // name
    final OgnlExpression parameter1 = parameterList.getParametersList().get(1);
    assertElementType(OgnlTypes.REFERENCE_EXPRESSION, parameter1);
  }

  // TODO method((ensureLoaded(1,2), name))
  //             ^

  public void testStaticMethodReference() {
    final OgnlMethodCallExpression expression = parse("@some@thing(1)");
    assertEquals("@some@thing", expression.getMethod().getText());
  }

  public void testQualifiedClassNameStaticMethodReference() {
    final OgnlMethodCallExpression expression = parse("@a.b.some@thing(1)");
    assertEquals("@a.b.some@thing", expression.getMethod().getText());
  }

  private OgnlMethodCallExpression parse(@Language(value = OgnlLanguage.ID,
    prefix = OgnlLanguage.EXPRESSION_PREFIX,
    suffix = OgnlLanguage.EXPRESSION_SUFFIX) final String expression) {
    return (OgnlMethodCallExpression)parseSingleExpression(expression);
  }
}