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
public class ComparisonBinaryExpressionPsiTest extends BinaryExpressionPsiTestCase {

  public void testComparisonLessThan() {
    assertConstantBinaryExpression("1 < 2", 1, OgnlTypes.LESS, 2);
  }

  public void testComparisonLessThanKeyword() {
    assertConstantBinaryExpression("1 lt 2", 1, OgnlTypes.LT_KEYWORD, 2);
  }

  public void testComparisonLessThanEqual() {
    assertConstantBinaryExpression("1 <= 2", 1, OgnlTypes.LESS_EQUAL, 2);
  }

  public void testComparisonLessThanEqualKeyword() {
    assertConstantBinaryExpression("1 lte 2", 1, OgnlTypes.LT_EQ_KEYWORD, 2);
  }

  public void testComparisonGreaterThan() {
    assertConstantBinaryExpression("1 > 2", 1, OgnlTypes.GREATER, 2);
  }

  public void testComparisonGreaterThanKeyword() {
    assertConstantBinaryExpression("1 gt 2", 1, OgnlTypes.GT_KEYWORD, 2);
  }

  public void testComparisonGreaterThanEqual() {
    assertConstantBinaryExpression("1 >= 2", 1, OgnlTypes.GREATER_EQUAL, 2);
  }

  public void testComparisonGreaterThanEqualKeyword() {
    assertConstantBinaryExpression("1 gte 2", 1, OgnlTypes.GT_EQ_KEYWORD, 2);
  }
}