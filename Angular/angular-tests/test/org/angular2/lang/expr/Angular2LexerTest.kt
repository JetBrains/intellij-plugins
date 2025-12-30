// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.expr

import com.intellij.lang.javascript.JSElementTypeServiceHelper.registerJSElementTypeServices
import com.intellij.lexer.Lexer
import com.intellij.mock.MockApplication
import org.angular2.Angular2TestUtil
import org.angular2.AngularLexerTestCase
import org.angular2.codeInsight.blocks.BLOCK_DEFER
import org.angular2.codeInsight.blocks.BLOCK_IF
import org.angular2.codeInsight.blocks.BLOCK_LET
import org.angular2.lang.expr.lexer.Angular2Lexer
import org.angular2.lang.html.Angular2TemplateSyntax
import org.jetbrains.annotations.NonNls
import java.io.File

open class Angular2LexerTest : AngularLexerTestCase() {

  protected open val templateSyntax: Angular2TemplateSyntax get() = Angular2TemplateSyntax.V_2

  private var lexerFactory: () -> Lexer = { Angular2Lexer(Angular2Lexer.RegularBinding(templateSyntax)) }

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

  fun testTemplateLiterals() {
    doFileTest("js")
  }

  override fun createLexer(): Lexer = lexerFactory()

  override fun getDirPath(): String {
    return Angular2TestUtil.getLexerTestDirPath() + "expr/lexer"
  }

  @Throws(Exception::class)
  override fun setUp() {
    val app = MockApplication.setUp(getTestRootDisposable())
    super.setUp()
    registerJSElementTypeServices(app, getTestRootDisposable())
  }

  private fun doBlockTest(name: String, index: Int) {
    doFileTest { Angular2Lexer(Angular2Lexer.BlockParameter(templateSyntax, name, index)) }
  }

  private fun doFileTest(factory: () -> Lexer) {
    val oldFactory = lexerFactory
    lexerFactory = factory
    doFileTest("js")
    lexerFactory = oldFactory
  }


  override fun getPathToTestDataFile(extension: String): String {
    val basePath = dirPath
    val fileName = getTestName(true) + extension
    // Iterate over syntax versions starting from the `templateSyntax` down to V_2
    return Angular2TemplateSyntax.entries.toList().asReversed().asSequence()
             .dropWhile { it != templateSyntax }
             .filter { it != Angular2TemplateSyntax.V_2_NO_EXPANSION_FORMS }
             .firstNotNullOfOrNull { syntax ->
               "${basePath}${syntax.dirSuffix}/$fileName".takeIf { File(it).exists() }
             }
           ?: "${basePath}${templateSyntax.dirSuffix}/$fileName"
  }

  private val Angular2TemplateSyntax.dirSuffix: String get() = if (this == Angular2TemplateSyntax.V_2) "" else "_$this"
}