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
public class ParenthesizedExpressionPsiTest extends PsiTestCase {

  public void testSimpleParentheses() {
    final OgnlExpression expression = parse("(3 + 4)");
    assertElementType(OgnlElementTypes.PARENTHESIZED_EXPRESSION, expression);
  }

  public void testNestedParentheses() {
    final OgnlExpression expression = parse("(3 + (4 * 5))");
    assertElementType(OgnlElementTypes.PARENTHESIZED_EXPRESSION, expression);
  }

  public void testNestedParenthesesWithMethodCall() {
    final OgnlExpression expression = parse("(3 + ( multiply(4, 5)))");
    assertElementType(OgnlElementTypes.PARENTHESIZED_EXPRESSION, expression);
  }

  private OgnlExpression parse(@Language(value = OgnlLanguage.ID,
                                         prefix = OgnlLanguage.EXPRESSION_PREFIX,
                                         suffix = OgnlLanguage.EXPRESSION_SUFFIX) final String expression) {
    return (OgnlExpression) parseSingleExpression(expression);
  }

}