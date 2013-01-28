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

package com.intellij.lang.ognl.psi.binaryexpression;

import com.intellij.lang.ognl.OgnlTypes;

/**
 * Arithmetic binary expressions.
 *
 * @author Yann C&eacute;bron
 */
public class ArithmeticBinaryExpressionPsiTest extends BinaryExpressionPsiTestCase {

  public void testMinus() {
    assertConstantBinaryExpression("1 - 2", 1, OgnlTypes.MINUS, 2);
  }

  public void testPlus() {
    assertConstantBinaryExpression("1 + 2", 1, OgnlTypes.PLUS, 2);
  }

  public void testMultiply() {
    assertConstantBinaryExpression("1 * 2", 1, OgnlTypes.MULTIPLY, 2);
  }

  public void testDivision() {
    assertConstantBinaryExpression("1 / 2", 1, OgnlTypes.DIVISION, 2);
  }

  public void testModulo() {
    assertConstantBinaryExpression("6 % 2", 6, OgnlTypes.MODULO, 2);
  }
}