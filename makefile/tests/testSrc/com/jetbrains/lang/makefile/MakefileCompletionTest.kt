package com.jetbrains.lang.makefile

import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightProjectFixture
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo

@TestApplication
class MakefileCompletionTest {
  private val projectFixture = codeInsightProjectFixture()
  private val myFixture by codeInsightFixture(projectFixture)

  private lateinit var testName: String

  @BeforeEach
  fun setUp(testInfo: TestInfo) {
    testName = PlatformTestUtil.getTestName(testInfo.testMethod.get().name, true)
    myFixture.testDataPath = BASE_TEST_DATA_PATH
  }

  @Test
  fun testSimple(): Unit = doTest("b", "c", "d", testFilename)
  @Test
  fun testTargets(): Unit = doTest("a", testFilename)
  @Test
  fun testAny(): Unit = doTest("include", "private", "define", "ifeq", "vpath", "ifndef", "ifneq", "override", "export", "undefine", "ifdef")
  @Test
  fun testFunctions(): Unit = doTest(*functions)
  @Test
  fun testVariables(): Unit = doTest("a", "b", "c", *functions)
  @Test
  fun testCurly(): Unit = doTest("a", "b", "c")
  @Test
  fun testComment(): Unit = doTest()

  fun doTest(vararg variants: String): Unit = myFixture.testCompletionVariants("$basePath/$testFilename", *variants)

  private val testFilename get() = "$testName.mk"
  private val basePath = "completion"

  private val functions = arrayOf("addsuffix", "realpath", "firstword", "origin", "dir", "error", "suffix", "wildcard", "findstring", "foreach", "file", "strip", "wordlist", "addprefix", "and", "guile", "abspath", "warning", "join", "if", "notdir", "value", "info", "or", "patsubst", "filter-out", "words", "subst", "sort", "call", "filter", "flavor", "basename", "eval", "lastword", "shell", "word")
}