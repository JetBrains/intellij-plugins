package org.jetbrains.webstorm.lang.parser

import org.junit.Test

class WebAssemblyTestFile : WebAssemblyTestBase("file") {
    @Test
    fun testAdd() = doTest()

    @Test
    fun testCall() = doTest()

    @Test
    fun testFail() = doTest()

    @Test
    fun testGlobal() = doTest()

    @Test
    fun testHighlighting() = doTest()

    @Test
    fun testLogger() = doTest()

    @Test
    fun testLogger2() = doTest()

    @Test
    fun testMemory() = doTest()

    @Test
    fun testShared0() = doTest()

    @Test
    fun testShared1() = doTest()

    @Test
    fun testSimple() = doTest()

    @Test
    fun testTable() = doTest()

    @Test
    fun testTable2() = doTest()

    @Test
    fun testWasmTable() = doTest()
}