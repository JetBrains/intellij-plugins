package org.jetbrains.astro.lang.sfc

import com.intellij.lexer.Lexer
import com.intellij.testFramework.LexerTestCase
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import org.jetbrains.annotations.NonNls
import org.jetbrains.astro.getAstroTestDataPath
import org.jetbrains.astro.lang.sfc.lexer.AstroLexerImpl
import kotlin.properties.Delegates

class AstroSfcLexerTest : LexerTestCase() {
  private var fixture: IdeaProjectTestFixture by Delegates.notNull()

  override fun setUp() {
    super.setUp()

    // needed for various XML extension points registration
    fixture = IdeaTestFixtureFactory.getFixtureFactory()
      .createLightFixtureBuilder(LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR, getTestName(false)).fixture
    fixture.setUp()
  }

  override fun tearDown() {
    try {
      fixture.tearDown()
    }
    catch (e: Throwable) {
      addSuppressedException(e)
    }
    finally {
      super.tearDown()
    }
  }

  fun testBasic1() = doTest("""
    |---
    |const a = 12
    |---
    |<div> { a } </div
  """)

  fun testBasic2() = doTest("""
    |Some comment
    |---
    |const a = new Text<Foo>("12")
    |---
    |Result is: { a }
  """)

  fun testEmptyFrontmatter1() = doTest("""
    |Some comment
    |------
    |const a = new Text<Foo>("12")
  """)

  fun testEmptyFrontmatter2() = doTest("""
    |------
    |const a = new Text<Foo>("12")
  """)

  fun testEmptyFrontmatter3() = doTest("""
    |------
  """)

  fun testNoFrontmatter1() = doTest("""
    |Foo Bar
    |Next line
  """)

  fun testNoFrontmatter2() = doTest("""
    |Foo Bar '12---' foo --- bar
  """)

  fun testNoFrontmatter3() = doTest("""
    |Foo Bar /*12 ---*/ foo --- bar
  """)

  fun testNoFrontmatter4() = doTest("""
    |Foo Bar //12*/ foo bar
    |foo --- bar
  """)

  fun testNoFrontmatter5() = doTest("""
    |Foo Bar /12/ foo --- bar
    |foo bar
  """)

  fun testNoFrontmatter6() = doTest("""
    |Foo Bar /12 foo --- bar
    |foo --- bar
  """)

  fun testNoFrontmatter7() = doTest("""
    |Foo Bar {12 --- 1} foo bar
    |foo bar
  """)

  fun testNoFrontmatter8() = doTest("""
    |Foo Bar <a --- > Foo
  """)

  override fun createLexer(): Lexer = AstroLexerImpl(fixture.project)

  override fun getDirPath() = "lang/sfc/lexer"

  override fun getPathToTestDataFile(extension: String?): String = getAstroTestDataPath() + "/$dirPath/" + getTestName(true) + extension

  override fun doTest(@NonNls text: String) {
    doTest(text, true)
  }

  private fun doTest(@NonNls text: String, checkRestartOnEveryToken: Boolean) {
    val withoutMargin = text.trimMargin()
    super.doTest(withoutMargin)
    if (checkRestartOnEveryToken) {
      checkCorrectRestartOnEveryToken(text)
    }
    else {
      checkCorrectRestart(withoutMargin)
    }
  }
}