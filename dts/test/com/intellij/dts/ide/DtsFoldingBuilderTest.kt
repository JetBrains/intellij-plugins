package com.intellij.dts.ide

import com.intellij.dts.DtsTestBase

class DtsFoldingBuilderTest : DtsTestBase() {
    override fun getBasePath(): String = "ide/folding"

    fun `test node`() = doTest()
    fun `test empty node`() = doTest()
    fun `test root node`() = doTest()
    fun `test empty root node`() = doTest()
    fun `test nested node`() = doTest()

    private fun doTest() = myFixture.testFolding(testFile)
}