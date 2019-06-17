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

package com.intellij.lang.ognl.formatting;

import com.intellij.lang.ognl.OgnlFileType;
import com.intellij.lang.ognl.OgnlTestUtils;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/**
 * Tests for {@link OgnlFormattingModelBuilder}.
 *
 * @author Yann C&eacute;bron
 */
public class OgnlFormattingTest extends BasePlatformTestCase {

  public void testComma() {
    doTest("{1 ,2}",
           "{1, 2}");
  }

  public void testQuestion() {
    doTest("true?1 : 2",
           "true ? 1 : 2");
  }

  public void testColon() {
    doTest("true?1:2",
           "true ? 1 : 2");
  }

  public void testWithinParentheses() {
    doTest("( a  )",
           "(a)");
  }

  public void testWithinBraces() {
    doTest("{ 1, 2 }",
           "{1, 2}");
  }

  public void testWithinBrackets() {
    doTest("id[ 5 ]",
           "id[5]");
  }

  public void testAdditionOperations() {
    doTest("1+2-3",
           "1 + 2 - 3");
  }

  public void testMultiplicationOperations() {
    doTest("1*2/3%4",
           "1 * 2 / 3 % 4");
  }

  public void testUnaryOperations() {
    doTest("{+ 1, - 2, ! 3, ~ 4}",
           "{+1, -2, !3, ~4}");
  }

  public void testEqualityOperations() {
    doTest("1==2!=3",
           "1 == 2 != 3");
  }

  public void testRelationalOperations() {
    doTest("1>2<3>=4<=5",
           "1 > 2 < 3 >= 4 <= 5");
  }

  public void testLogicalOperations() {
    doTest("1||2&&3",
           "1 || 2 && 3");
  }

  public void testShiftOperations() {
    doTest("1>>2<<3>>>4",
           "1 >> 2 << 3 >>> 4");
  }

  public void testBitwiseOperations() {
    doTest("1|2&3^4",
           "1 | 2 & 3 ^ 4");
  }

  private void doTest(final String before,
                      final String after) {
    myFixture.configureByText(OgnlFileType.INSTANCE, OgnlTestUtils.createExpression(before));
    WriteCommandAction.runWriteCommandAction(null, () -> {
      CodeStyleManager.getInstance(myFixture.getProject()).reformat(myFixture.getFile());
    });
    myFixture.checkResult(OgnlTestUtils.createExpression(after));
  }
}