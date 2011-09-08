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

package com.intellij.lang.ognl.psi.binaryexpression;

import com.intellij.lang.ognl.parsing.OgnlElementTypes;
import com.intellij.lang.ognl.psi.OgnlBinaryExpression;
import com.intellij.lang.ognl.psi.OgnlTokenTypes;

/**
 * @author Yann C&eacute;bron
 */
public class InNotInBinaryExpressionPsiTest extends BinaryExpressionPsiTestCase {

  public void testSimpleIn() {
    final OgnlBinaryExpression binaryExpression = parse("a in {1,2}");
    assertEquals(OgnlTokenTypes.IN_KEYWORD, binaryExpression.getOperationSign());
    assertElementType(OgnlElementTypes.REFERENCE_EXPRESSION, binaryExpression.getLeftOperand());
    assertElementType(OgnlElementTypes.SEQUENCE_EXPRESSION, binaryExpression.getRightOperand());
  }

  public void testSimpleNotIn() {
    final OgnlBinaryExpression binaryExpression = parse("a not in {1,2}");
    assertEquals(OgnlTokenTypes.NOT_IN_KEYWORD, binaryExpression.getOperationSign());
    assertElementType(OgnlElementTypes.REFERENCE_EXPRESSION, binaryExpression.getLeftOperand());
    assertElementType(OgnlElementTypes.SEQUENCE_EXPRESSION, binaryExpression.getRightOperand());
  }

}