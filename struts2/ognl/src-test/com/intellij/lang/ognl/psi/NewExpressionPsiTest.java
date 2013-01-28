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
import org.intellij.lang.annotations.Language;

/**
 * {@link OgnlNewExpression}.
 *
 * @author Yann C&eacute;bron
 */
public class NewExpressionPsiTest extends PsiTestCase {

  public void testClassnameWithNoParameters() {
    final OgnlNewExpression expression = parse("new Something()");
  }

  public void testQualifiedClassnameWithNoParameters() {
    final OgnlNewExpression expression = parse("new java.util.ArrayList()");
  }

  public void testClassnameWithOneParameter() {
    final OgnlNewExpression expression = parse("new Integer(1)");
  }

  public void testClassnameWithMultipleParameters() {
    final OgnlNewExpression expression = parse("new Something(1, 2)");
  }

  public void testIntArrayEmpty() {
    final OgnlNewExpression expression = parse("new int[0]");
  }

  public void testIntArrayWithSequence() {
    final OgnlNewExpression expression = parse("new int[] {1, 2}");
  }

  private OgnlNewExpression parse(@Language(value = OgnlLanguage.ID,
                                            prefix = OgnlLanguage.EXPRESSION_PREFIX,
                                            suffix = OgnlLanguage.EXPRESSION_SUFFIX) final String expression) {
    return (OgnlNewExpression)parseSingleExpression(expression);
  }
}