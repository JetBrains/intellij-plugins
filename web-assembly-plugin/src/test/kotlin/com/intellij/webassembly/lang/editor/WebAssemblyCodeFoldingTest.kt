package com.intellij.webassembly.lang.editor

import com.intellij.platform.testFramework.junit5.codeInsight.fixture.sharedCodeInsightFixture
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.sharedCodeInsightProjectFixture
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.testNameFixture
import org.junit.jupiter.api.Test

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/contrib/web-assembly-plugin/src/test/resources/editor/codeFolding")
class WebAssemblyCodeFoldingTest {
    companion object {
        private val projectFixture = sharedCodeInsightProjectFixture()
    }

    private val myFixture by sharedCodeInsightFixture(projectFixture)
    private val testName by testNameFixture(false)

    @Test
    fun testBr(): Unit = testCodeFolding(testName)
    @Test
    fun testBrifNamed(): Unit = testCodeFolding(testName)
    @Test
    fun testBrLoop(): Unit = testCodeFolding(testName)
    @Test
    fun testBrtable(): Unit = testCodeFolding(testName)
    @Test
    fun testExprBrif(): Unit = testCodeFolding(testName)
    @Test
    fun testHighlighting(): Unit = testCodeFolding(testName)
    @Test
    fun testIf(): Unit = testCodeFolding(testName)
    @Test
    fun testIfMultiNamed(): Unit = testCodeFolding(testName)
    @Test
    fun testIfReturn(): Unit = testCodeFolding(testName)
    @Test
    fun testIfThenBr(): Unit = testCodeFolding(testName)
    @Test
    fun testIfThenBrNamed(): Unit = testCodeFolding(testName)
    @Test
    fun testIfThenElseBr(): Unit = testCodeFolding(testName)
    @Test
    fun testIfThenElseBrNamed(): Unit = testCodeFolding(testName)
    @Test
    fun testLoop(): Unit = testCodeFolding(testName)
    @Test
    fun testLoopMultiNamed(): Unit = testCodeFolding(testName)

    private fun testCodeFolding(testName: String) {
        myFixture.testFoldingWithCollapseStatus("${myFixture.testDataPath}/${testName}.wat")
    }
}
