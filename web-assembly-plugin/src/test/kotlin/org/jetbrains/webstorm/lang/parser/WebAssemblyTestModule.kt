package org.jetbrains.webstorm.lang.parser

import org.junit.Test

class WebAssemblyTestModule : WebAssemblyTestBase("module") {
    @Test
    fun testDataOffset() = doTest()
    @Test
    fun testElemOffset() = doTest()
    @Test
    fun testExportFuncMulti() = doTest()
    @Test
    fun testExportFuncNamed() = doTest()
    @Test
    fun testExportFunc() = doTest()
    @Test
    fun testExportGlobal() = doTest()
    @Test
    fun testExportMemoryMulti() = doTest()
    @Test
    fun testExportMemory() = doTest()
    @Test
    fun testExportMemoryInline() = doTest()
    @Test
    fun testExportMemoryInlineSeveral() = doTest()
    @Test
    fun testExportTable() = doTest()
    @Test
    fun testGlobalExnref() = doTest()
    @Test
    fun testGlobal() = doTest()
    @Test
    fun testImportFuncNoParam() = doTest()
    @Test
    fun testImportFunc() = doTest()
    @Test
    fun testImportFuncType() = doTest()
    @Test
    fun testImportGlobalGetglobal() = doTest()
    @Test
    fun testImportGlobal() = doTest()
    @Test
    fun testImportMemory() = doTest()
    @Test
    fun testImportMutableGlobal() = doTest()
    @Test
    fun testImportTable() = doTest()
    @Test
    fun testMemoryInitMaxSize() = doTest()
    @Test
    fun testMemoryInitSize() = doTest()
    @Test
    fun testMemorySegment1() = doTest()
    @Test
    fun testMemorySegmentLong() = doTest()
    @Test
    fun testMemorySegmentMany() = doTest()
    @Test
    fun testMemorySegmentMultiString() = doTest()
    @Test
    fun testMemorySegmentPassive() = doTest()
    @Test
    fun testModuleEmpty() = doTest()
    @Test
    fun testReferenceTypesDisabled() = doTest()
    @Test
    fun testStartNamed() = doTest()
    @Test
    fun testStart() = doTest()
    @Test
    fun testTableElemExpr() = doTest()
    @Test
    fun testTableElemVar() = doTest()
    @Test
    fun testTableNamed() = doTest()
    @Test
    fun testTable() = doTest()
    @Test
    fun testTypeEmptyParam() = doTest()
    @Test
    fun testTypeEmpty() = doTest()
    @Test
    fun testTypeMultiParam() = doTest()
    @Test
    fun testTypeNoParam() = doTest()
    @Test
    fun testType() = doTest()

    @Test
    fun testBadElemRedefinition() = doTest()
    @Test
    fun testBadExportFuncEmpty() = doTest()
    @Test
    fun testBadExportFuncName() = doTest()
    @Test
    fun testBadExportFuncNoString() = doTest()
    @Test
    fun testBadExportFuncTooMany() = doTest()
    @Test
    fun testBadImportFuncNotParam() = doTest()
    @Test
    fun testBadImportFuncNotResult() = doTest()
    @Test
    fun testBadImportFuncOneString() = doTest()
    @Test
    fun testBadMemoryEmpty() = doTest()
    @Test
    fun testBadMemoryInitSizeNegative() = doTest()
    @Test
    fun testBadMemoryInitSize() = doTest()
    @Test
    fun testBadMemoryMaxSizeNegative() = doTest()
    @Test
    fun testBadMemoryMaxSize() = doTest()
    @Test
    fun testBadMemorySegmentAddress() = doTest()
    @Test
    fun testBadModuleNoClose() = doTest()
    @Test
    fun testBadTableElem() = doTest()
}