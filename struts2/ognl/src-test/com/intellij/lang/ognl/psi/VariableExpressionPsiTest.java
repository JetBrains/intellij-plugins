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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiType;
import org.intellij.lang.annotations.Language;

/**
 * {@link OgnlVariableExpression}.
 *
 * @author Yann C&eacute;bron
 */
public class VariableExpressionPsiTest extends PsiTestCase {

  enum ResolveTarget {
    NULL,
    ELEMENT,
    SELF,
    TYPE
  }

  public void testUndefinedVariableReference() {
    doTest("undefinedVariable", ResolveTarget.NULL, null);
  }

  public void testVariableContext() {
    doTest("context", ResolveTarget.TYPE, "java.util.Map<String,Object>");
  }

  public void testVariableRoot() {
    doTest("root", ResolveTarget.SELF, null);
  }

  public void testVariableThis() {
    doTest("this", ResolveTarget.SELF, null);
  }

  public void testReferenceAfterVariable() {
    parseSingleExpression("#root.something.property");
  }

  public void testMethodCallAfterVariable() {
    parseSingleExpression("#root.something.method()");
  }

  public void testMethodCallWithParamsAfterVariable() {
    parseSingleExpression("#root.something.method(1, 'a')");
  }

  private OgnlVariableExpression parse(@Language(value = OgnlLanguage.ID,
                                                 prefix = OgnlLanguage.EXPRESSION_PREFIX,
                                                 suffix = OgnlLanguage.EXPRESSION_SUFFIX) final String expression) {
    return (OgnlVariableExpression)parseSingleExpression(expression);
  }

  private void doTest(String name,
                      ResolveTarget resolveTarget,
                      Object expectedResolveTarget) {
    final OgnlVariableExpression expression = parse("#" + name);

    final PsiReference reference = expression.getReference();
    assertNotNull(reference);
    assertEquals(name, reference.getCanonicalText());

    final PsiElement resolveElement = reference.resolve();
    switch (resolveTarget) {
      case NULL -> assertNull(resolveElement);
      case ELEMENT -> assertEquals(expectedResolveTarget, resolveElement);
      case SELF -> assertEquals(expression.getNavigationElement(), resolveElement);
      case TYPE -> {
        final String expectedType = (String)expectedResolveTarget;
        final PsiType type = getJavaFacade().getElementFactory().createTypeFromText(expectedType, expression);
        assertNotNull(type);
        assertEquals(expectedType, type.getCanonicalText());
      }
    }
  }
}