package org.jetbrains.webstorm.lang.parser

import org.junit.Test

class WebAssemblyTestCommon : WebAssemblyTestBase("common") {
    @Test
    fun testBasic() = doTest()
    @Test
    fun testEmptyFile() = doTest()
    @Test
    fun testExportMutableGlobal() = doTest()
    @Test
    fun testForceColor() = doTest()
    @Test
    fun testLineComment() = doTest()
    @Test
    fun testNestedComments() = doTest()
    @Test
    fun testStdin() = doTest()
    @Test
    fun testStringEscape() = doTest()
    @Test
    fun testStringHex() = doTest()

    @Test
    fun testBadCrlf() = doTest()
    @Test
    fun testBadErrorLongLine() = doTest()
    @Test
    fun testBadErrorLongToken() = doTest()
    @Test
    fun testBadInputCommand() = doTest()
    @Test
    fun testBadOutputCommand() = doTest()
    @Test
    fun testBadSingleSemicolon() = doTest()
    @Test
    fun testBadStringEof() = doTest()
    @Test
    fun testBadStringEscape() = doTest()
    @Test
    fun testBadStringHexEscape() = doTest()
    @Test
    fun testBadToplevel() = doTest()
}