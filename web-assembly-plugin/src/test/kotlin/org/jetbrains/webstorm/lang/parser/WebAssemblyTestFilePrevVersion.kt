package org.jetbrains.webstorm.lang.parser

import org.junit.Test

class WebAssemblyTestFilePrevVersion : WebAssemblyTestBase("file_v1.0") {
    @Test
    fun testAdd() = doTest()
    @Test
    fun testShared0() = doTest()
    @Test
    fun testShared1() = doTest()
    @Test
    fun testTable() = doTest()
    @Test
    fun testTable2() = doTest()
    @Test
    fun testWasmTable() = doTest()
}