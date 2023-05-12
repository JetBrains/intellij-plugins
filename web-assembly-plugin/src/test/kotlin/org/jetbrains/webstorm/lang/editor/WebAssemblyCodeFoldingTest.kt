package org.jetbrains.webstorm.lang.editor

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test

class WebAssemblyCodeFoldingTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String = "src/test/resources/editor/codeFolding"

    @Test
    fun testBr() = testCodeFolding(getTestName(false))
    @Test
    fun testBrifNamed() = testCodeFolding(getTestName(false))
    @Test
    fun testBrLoop() = testCodeFolding(getTestName(false))
    @Test
    fun testBrtable() = testCodeFolding(getTestName(false))
    @Test
    fun testExprBrif() = testCodeFolding(getTestName(false))
    @Test
    fun testHighlighting() = testCodeFolding(getTestName(false))
    @Test
    fun testIf() = testCodeFolding(getTestName(false))
    @Test
    fun testIfMultiNamed() = testCodeFolding(getTestName(false))
    @Test
    fun testIfReturn() = testCodeFolding(getTestName(false))
    @Test
    fun testIfThenBr() = testCodeFolding(getTestName(false))
    @Test
    fun testIfThenBrNamed() = testCodeFolding(getTestName(false))
    @Test
    fun testIfThenElseBr() = testCodeFolding(getTestName(false))
    @Test
    fun testIfThenElseBrNamed() = testCodeFolding(getTestName(false))
    @Test
    fun testLoop() = testCodeFolding(getTestName(false))
    @Test
    fun testLoopMultiNamed() = testCodeFolding(getTestName(false))

    private fun testCodeFolding(testName: String) {
        myFixture.testFoldingWithCollapseStatus("${testDataPath}/${testName}.wat")
    }
}