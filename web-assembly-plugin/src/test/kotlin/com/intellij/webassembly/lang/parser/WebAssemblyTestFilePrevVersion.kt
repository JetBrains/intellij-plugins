package com.intellij.webassembly.lang.parser

import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
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