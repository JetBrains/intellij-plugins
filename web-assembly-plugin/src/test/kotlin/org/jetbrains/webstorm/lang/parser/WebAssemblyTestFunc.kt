package org.jetbrains.webstorm.lang.parser

import org.junit.Test

class WebAssemblyTestFunc : WebAssemblyTestBase("func") {
    @Test
    fun testFuncNamed() = doTest()
    @Test
    fun testLocal() = doTest()
    @Test
    fun testLocalEmpty() = doTest()
    @Test
    fun testLocalExnref() = doTest()
    @Test
    fun testLocalMulti() = doTest()
    @Test
    fun testNoSpace() = doTest()
    @Test
    fun testParamBinding() = doTest()
    @Test
    fun testParamExnref() = doTest()
    @Test
    fun testParamMulti() = doTest()
    @Test
    fun testParamType1() = doTest()
    @Test
    fun testParamType2() = doTest()
    @Test
    fun testResult() = doTest()
    @Test
    fun testResultEmpty() = doTest()
    @Test
    fun testResultExnref() = doTest()
    @Test
    fun testResultMulti() = doTest()
    @Test
    fun testSig() = doTest()
    @Test
    fun testSigMatch() = doTest()

    @Test
    fun testBadFuncName() = doTest()
    @Test
    fun testBadLocalBinding() = doTest()
    @Test
    fun testBadLocalBindingNoType() = doTest()
    @Test
    fun testBadLocalName() = doTest()
    @Test
    fun testBadLocalType() = doTest()
    @Test
    fun testBadLocalTypeList() = doTest()
    @Test
    fun testBadParam() = doTest()
    @Test
    fun testBadParamBinding() = doTest()
    @Test
    fun testBadParamName() = doTest()
    @Test
    fun testBadParamTypeList() = doTest()
    @Test
    fun testBadResultType() = doTest()
}