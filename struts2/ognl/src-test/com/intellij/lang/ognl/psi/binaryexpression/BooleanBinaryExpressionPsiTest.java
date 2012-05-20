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

import com.intellij.lang.ognl.psi.OgnlBinaryExpression;
import com.intellij.lang.ognl.psi.OgnlElement;
import com.intellij.lang.ognl.psi.OgnlTokenTypes;
import com.intellij.psi.PsiType;

/**
 * @author Yann C&eacute;bron
 */
public class BooleanBinaryExpressionPsiTest extends BinaryExpressionPsiTestCase {

  public void testBooleanAnd() {
    assertConstantBinaryExpression("true && false", true, OgnlTokenTypes.AND_AND, false);
  }

  public void testBooleanAndKeyword() {
    assertConstantBinaryExpression("true and false", true, OgnlTokenTypes.AND_KEYWORD, false);
  }

  public void testBooleanOr() {
    assertConstantBinaryExpression("true || false", true, OgnlTokenTypes.OR_OR, false);
  }

  public void testBooleanOrKeyword() {
    assertConstantBinaryExpression("true or false", true, OgnlTokenTypes.OR_KEYWORD, false);
  }

  public void testExpressionType() {
    final OgnlElement element = parseSingleExpression("true or false");
    assertInstanceOf(element, OgnlBinaryExpression.class);
    assertEquals(PsiType.BOOLEAN, ((OgnlBinaryExpression) element).getType());
  }

}