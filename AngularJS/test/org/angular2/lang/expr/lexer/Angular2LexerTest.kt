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
package org.angular2.lang.expr.lexer;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.application.PathManager;
import com.intellij.testFramework.LexerTestCase;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NonNls;

public class Angular2LexerTest extends LexerTestCase {
  public void testIdent() {
    doFileTest("js");
  }

  public void testKey_value() {
    doFileTest("js");
  }

  public void testExpr() {
    doFileTest("js");
  }

  public void testKeyword() {
    doFileTest("js");
  }

  public void testNumber() {
    doFileTest("js");
  }

  public void testString() {
    doFileTest("js");
  }

  @Override
  protected Lexer createLexer() {
    return new Angular2Lexer();
  }

  @Override
  protected String getDirPath() {
    return AngularTestUtil.getBaseTestDataPath(Angular2LexerTest.class).substring(PathManager.getHomePath().length());
  }

  @Override
  protected void doTest(@NonNls String text) {
    super.doTest(text);
    checkCorrectRestart(text);
  }
}