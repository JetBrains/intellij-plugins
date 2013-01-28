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
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiReference;
import org.intellij.lang.annotations.Language;

/**
 * {@link OgnlVariableExpression}.
 *
 * @author Yann C&eacute;bron
 */
public class VariableExpressionPsiTest extends PsiTestCase {

  public void testVariableReference() {
    final OgnlVariableExpression expression = parse("#exp");
    final ItemPresentation presentation = expression.getPresentation();
    assertNotNull(presentation);
    assertEquals("exp", presentation.getPresentableText());

    final PsiReference reference = expression.getReference();
    assertNotNull(reference);
    assertEquals("exp", reference.getCanonicalText());
    assertEquals(expression.getNavigationElement(), reference.resolve());
  }

  public void testVariableThis() {
    final OgnlVariableExpression expression = parse("#this");
    final ItemPresentation presentation = expression.getPresentation();
    assertNotNull(presentation);
    assertEquals("this", presentation.getPresentableText());

    final PsiReference reference = expression.getReference();
    assertNotNull(reference);
    assertEquals("this", reference.getCanonicalText());
    assertEquals(expression.getNavigationElement(), reference.resolve());
  }

  private OgnlVariableExpression parse(@Language(value = OgnlLanguage.ID,
                                                 prefix = OgnlLanguage.EXPRESSION_PREFIX,
                                                 suffix = OgnlLanguage.EXPRESSION_SUFFIX) final String expression) {
    return (OgnlVariableExpression)parseSingleExpression(expression);
  }
}