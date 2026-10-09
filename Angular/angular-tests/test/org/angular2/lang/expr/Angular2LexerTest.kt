// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.expr

import com.intellij.lang.javascript.JSElementTypeServiceHelper.registerJSElementTypeServices
import com.intellij.lexer.Lexer
import com.intellij.mock.MockApplication
import com.intellij.openapi.Disposable
import com.intellij.openapi.util.text.StringUtil
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.lexerFixture
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.junit5.TestDisposable
import com.intellij.testFramework.junit5.fixture.TestFixtures
import org.angular2.Angular2TestUtil
import org.angular2.codeInsight.blocks.BLOCK_DEFER
import org.angular2.codeInsight.blocks.BLOCK_IF
import org.angular2.codeInsight.blocks.BLOCK_LET
import org.angular2.lang.expr.lexer.Angular2Lexer
import org.angular2.lang.html.Angular2TemplateSyntax
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.readText

@TestFixtures
open class Angular2LexerTest {

  protected open val templateSyntax: Angular2TemplateSyntax get() = Angular2TemplateSyntax.V_2

  private var lexerFactory: () -> Lexer = { Angular2Lexer(Angular2Lexer.RegularBinding(templateSyntax)) }

  private val lexer by lexerFixture(DIR_PATH) { lexerFactory() }

  @TestDisposable
  lateinit var disposable: Disposable

  @Test
  fun testIdent() {
    doFileTest()
  }

  @Test
  fun testKey_value() {
    doFileTest()
  }

  @Test
  fun testExpr() {
    doFileTest()
  }

  @Test
  fun testKeyword() {
    doFileTest()
  }

  @Test
  fun testNumber() {
    doFileTest()
  }

  @Test
  fun testString() {
    doFileTest()
  }

  @Test
  fun testIfBlockPrimaryExpression() {
    doBlockTest(BLOCK_IF, 0)
  }

  @Test
  fun testIfBlockAsParameter() {
    doBlockTest(BLOCK_IF, 1)
  }

  @Test
  fun testIfBlockUnknownParameter() {
    doBlockTest(BLOCK_IF, 1)
  }

  @Test
  fun testDeferBlockParameter1() {
    doBlockTest(BLOCK_DEFER, 1)
  }

  @Test
  fun testDeferBlockParameter2() {
    doBlockTest(BLOCK_DEFER, 1)
  }

  @Test
  fun testDeferBlockParameter3() {
    doBlockTest(BLOCK_DEFER, 1)
  }

  @Test
  fun testDeferBlockParameter4() {
    doBlockTest(BLOCK_DEFER, 1)
  }

  @Test
  fun testLetBlock() {
    doBlockTest(BLOCK_LET, 0)
  }

  @Test
  fun testTemplateLiterals() {
    doFileTest()
  }

  @Test
  fun testRegex() {
    doPerLineTest()
  }

  @BeforeEach
  fun setUp() {
    val app = MockApplication.setUp(disposable)
    registerJSElementTypeServices(app, disposable)
  }

  private fun doBlockTest(name: String, index: Int) {
    doFileTest { Angular2Lexer(Angular2Lexer.BlockParameter(templateSyntax, name, index)) }
  }

  private fun doFileTest(factory: () -> Lexer) {
    val oldFactory = lexerFactory
    lexerFactory = factory
    doFileTest()
    lexerFactory = oldFactory
  }

  private fun doFileTest() {
    val text = loadTestDataFile()
    PlatformTestUtil.assertSameLinesWithFile(getPathToTestDataFile(".txt"), lexer.printTokens(text, 0))
    lexer.checkCorrectRestart(text)
  }

  private fun doPerLineTest() {
    val result = loadTestDataFile()
      .split("\n")
      .joinToString("\n") {
        if (it.startsWith("//"))
          it
        else
          "// $it\n" +
          lexer.printTokens(it, 0)
      }
    PlatformTestUtil.assertSameLinesWithFile(getPathToTestDataFile(".txt"), result)
  }

  private fun loadTestDataFile(): String =
    StringUtil.convertLineSeparators(Path(getPathToTestDataFile(".js")).readText().trim())

  private fun getPathToTestDataFile(extension: String): String {
    val basePath = DIR_PATH
    val fileName = lexer.testName + extension
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

  private companion object {
    val DIR_PATH: String = Angular2TestUtil.getLexerTestDirPath() + "expr/lexer"
  }
}