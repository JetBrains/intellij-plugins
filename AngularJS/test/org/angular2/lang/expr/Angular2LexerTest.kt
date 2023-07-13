// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.expr

import com.intellij.lexer.Lexer
import com.intellij.testFramework.LexerTestCase
import org.angular2.lang.expr.lexer.Angular2Lexer
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
    return AngularTestUtil.getLexerTestDirPath() + "expr/lexer"
  }

  override fun doTest(text: @NonNls String) {
    super.doTest(text)
    checkCorrectRestart(text)
  }
}