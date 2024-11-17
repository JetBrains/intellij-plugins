// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.expr

import com.intellij.lexer.Lexer
import com.intellij.testFramework.LexerTestCase
import org.angular2.Angular2TestUtil
import org.angular2.codeInsight.blocks.BLOCK_DEFER
import org.angular2.codeInsight.blocks.BLOCK_IF
import org.angular2.codeInsight.blocks.BLOCK_LET
import org.angular2.lang.expr.lexer.Angular2Lexer
import org.jetbrains.annotations.NonNls

class Angular2LexerTest : LexerTestCase() {
  private var lexerFactory: () -> Lexer = { Angular2Lexer(Angular2Lexer.RegularBinding) }

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

  fun testIfBlockPrimaryExpression() {
    doBlockTest(BLOCK_IF, 0)
  }

  fun testIfBlockAsParameter() {
    doBlockTest(BLOCK_IF, 1)
  }

  fun testIfBlockUnknownParameter() {
    doBlockTest(BLOCK_IF, 1)
  }

  fun testDeferBlockParameter1() {
    doBlockTest(BLOCK_DEFER, 1)
  }

  fun testDeferBlockParameter2() {
    doBlockTest(BLOCK_DEFER, 1)
  }

  fun testDeferBlockParameter3() {
    doBlockTest(BLOCK_DEFER, 1)
  }

  fun testDeferBlockParameter4() {
    doBlockTest(BLOCK_DEFER, 1)
  }

  fun testLetBlock() {
    doBlockTest(BLOCK_LET, 0)
  }

  override fun createLexer(): Lexer = lexerFactory()

  override fun getDirPath(): String {
    return Angular2TestUtil.getLexerTestDirPath() + "expr/lexer"
  }

  override fun doTest(text: @NonNls String) {
    super.doTest(text)
    checkCorrectRestart(text)
  }

  private fun doBlockTest(name: String, index: Int) {
    doFileTest { Angular2Lexer(Angular2Lexer.BlockParameter(name, index)) }
  }

  private fun doFileTest(factory: () -> Lexer) {
    val oldFactory = lexerFactory
    lexerFactory = factory
    doFileTest("js")
    lexerFactory = oldFactory
  }
}