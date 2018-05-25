// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.intellij.lang.ognl.lexer;

import com.intellij.lang.ognl.OgnlTestUtils;
import com.intellij.lang.ognl.OgnlTypes;
import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.TokenSet;
import com.intellij.testFramework.LexerTestCase;

public class OgnlLexerTest extends LexerTestCase {

  @Override
  protected void doTest(String text) {
    super.doTest(text);

    checkCorrectRestart(text);
    checkZeroState(text, TokenSet.create(OgnlTypes.EXPRESSION_START));
  }

  public void testNestedBraces() {
    doTest("%{ { { } } }");
  }

  public void testNestedBracesWithoutExpression() {
    doTest("{ { } }");
  }

  public void testNestedModuloAndCurly() {
    doTest("%{ %{ }}");
  }

  public void testTwoRightCurly() {
    doTest("${ } }");
  }

  @Override
  protected Lexer createLexer() {
    return new OgnlLexer();
  }

  @Override
  protected String getDirPath() {
    return OgnlTestUtils.OGNL_TEST_DATA + "/lexer";
  }
}
