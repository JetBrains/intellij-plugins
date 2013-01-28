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
 * @author Yann C&eacute;bron
 */
public class BitwiseShiftBinaryExpressionPsiTest extends BinaryExpressionPsiTestCase {

  public void testBitwiseShiftLeft() {
    assertConstantBinaryExpression("1 << 2", 1, OgnlTypes.SHIFT_LEFT, 2);
  }

  public void testBitwiseShiftLeftKeyword() {
    assertConstantBinaryExpression("1 shl 2", 1, OgnlTypes.SHIFT_LEFT_KEYWORD, 2);
  }

  public void testBitwiseShiftRight() {
    assertConstantBinaryExpression("1 >> 2", 1, OgnlTypes.SHIFT_RIGHT, 2);
  }

  public void testBitwiseShiftRightKeyword() {
    assertConstantBinaryExpression("1 shr 2", 1, OgnlTypes.SHIFT_RIGHT_KEYWORD, 2);
  }

  public void testBitwiseShiftRightLogical() {
    assertConstantBinaryExpression("1 >>> 2", 1, OgnlTypes.SHIFT_RIGHT_LOGICAL, 2);
  }

  public void testBitwiseShiftRightLogicalKeyword() {
    assertConstantBinaryExpression("1 ushr 2", 1, OgnlTypes.SHIFT_RIGHT_LOGICAL_KEYWORD, 2);
  }
}