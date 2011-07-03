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
 * {@link OgnlElementTypes#INDEXED_EXPRESSION}.
 *
 * @author Yann C&eacute;bron
 */
public class IndexedExpressionPsiTest extends PsiTestCase {

  public void testSimpleIntegerIndex() {
    final OgnlElement element = parse("a[0]");

    assertEquals(OgnlElementTypes.INDEXED_EXPRESSION, element.getNode().getElementType());
  }

  public void testExpressionIntegerIndex() {
    final OgnlElement element = parse("a[1+2]");
    assertEquals(OgnlElementTypes.INDEXED_EXPRESSION, element.getNode().getElementType());
  }

  private OgnlElement parse(final String expression) {
    return parseSingleExpression(expression);
  }

}