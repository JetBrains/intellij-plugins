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
package org.angular2.lang.expr.lexer

import com.intellij.lexer.Lexer
import com.intellij.openapi.application.PathManager
import com.intellij.testFramework.LexerTestCase
import org.angularjs.AngularTestUtil
import org.jetbrains.annotations.NonNls

class Angular2LexerTest : LexerTestCase() {
  fun testIdent() {
    doFileTest("js")
  }

  fun testKey_value() {
    doFileTest("js")
  }

  fun testExpr() {
    doFileTest("js")
  }

  fun testKeyword() {
    doFileTest("js")
  }

  fun testNumber() {
    doFileTest("js")
  }

  fun testString() {
    doFileTest("js")
  }

  override fun createLexer(): Lexer {
    return Angular2Lexer()
  }

  override fun getDirPath(): String {
    return AngularTestUtil.getBaseTestDataPath(Angular2LexerTest::class.java).substring(PathManager.getHomePath().length)
  }

  override fun doTest(text: @NonNls String) {
    super.doTest(text)
    checkCorrectRestart(text)
  }
}