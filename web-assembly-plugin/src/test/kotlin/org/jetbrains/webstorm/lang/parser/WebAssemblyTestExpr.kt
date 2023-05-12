package org.jetbrains.webstorm.lang.parser

import org.junit.Test

class WebAssemblyTestExpr : WebAssemblyTestBase("expr") {
    @Test
    fun testBinary() = doTest()
    @Test
    fun testBlock() = doTest()
    @Test
    fun testBlockMulti() = doTest()
    @Test
    fun testBlockMultiNamed() = doTest()
    @Test
    fun testBlockNamed() = doTest()
    @Test
    fun testBlockReturn() = doTest()
    @Test
    fun testBr() = doTest()
    @Test
    fun testBrBlock() = doTest()
    @Test
    fun testBrif() = doTest()
    @Test
    fun testBrifNamed() = doTest()
    @Test
    fun testBrLoop() = doTest()
    @Test
    fun testBrNamed() = doTest()
    @Test
    fun testBrtable() = doTest()
    @Test
    fun testBrtableMulti() = doTest()
    @Test
    fun testBrtableNamed() = doTest()
    @Test
    fun testBulkMemoryDisabled() = doTest()
    @Test
    fun testBulkMemoryNamed() = doTest()
    @Test
    fun testCall() = doTest()
    @Test
    fun testCallDefinedLater() = doTest()
    @Test
    fun testCallimport() = doTest()
    @Test
    fun testCallimportNamed() = doTest()
    @Test
    fun testCallimportType() = doTest()
    @Test
    fun testCallindirect() = doTest()
    @Test
    fun testCallindirectNamed() = doTest()
    @Test
    fun testCallNamed() = doTest()
    @Test
    fun testCallNamePrefix() = doTest()
    @Test
    fun testCast() = doTest()
    @Test
    fun testCompare() = doTest()
    @Test
    fun testConst() = doTest()
    @Test
    fun testConvert() = doTest()
    @Test
    fun testConvertSat() = doTest()
    @Test
    fun testCurrentMemory() = doTest()
    @Test
    fun testDrop() = doTest()
    @Test
    fun testExprBr() = doTest()
    @Test
    fun testExprBrif() = doTest()
    @Test
    fun testGetglobal() = doTest()
    @Test
    fun testGetglobalNamed() = doTest()
    @Test
    fun testGetlocal() = doTest()
    @Test
    fun testGetlocalIndexAfterParam() = doTest()
    @Test
    fun testGetlocalIndexMixedNamedUnnamed() = doTest()
    @Test
    fun testGetlocalNamed() = doTest()
    @Test
    fun testGetlocalParam() = doTest()
    @Test
    fun testGetlocalParamNamed() = doTest()
    @Test
    fun testGrowMemory() = doTest()
    @Test
    fun testIf() = doTest()
    @Test
    fun testIfMulti() = doTest()
    @Test
    fun testIfMultiNamed() = doTest()
    @Test
    fun testIfReturn() = doTest()
    @Test
    fun testIfThenBr() = doTest()
    @Test
    fun testIfThenBrNamed() = doTest()
    @Test
    fun testIfThenElse() = doTest()
    @Test
    fun testIfThenElseBr() = doTest()
    @Test
    fun testIfThenElseBrNamed() = doTest()
    @Test
    fun testIfThenElseList() = doTest()
    @Test
    fun testLoad() = doTest()
    @Test
    fun testLoadAligned() = doTest()
    @Test
    fun testLoadOffset() = doTest()
    @Test
    fun testLoop() = doTest()
    @Test
    fun testLoopMulti() = doTest()
    @Test
    fun testLoopMultiNamed() = doTest()
    @Test
    fun testLoopNamed() = doTest()
    @Test
    fun testMemoryCopy() = doTest()
    @Test
    fun testMemoryDrop() = doTest()
    @Test
    fun testMemoryFill() = doTest()
    @Test
    fun testMemoryInit() = doTest()
    @Test
    fun testNop() = doTest()
    @Test
    fun testReferenceTypes() = doTest()
    @Test
    fun testReferenceTypesCallIndirect() = doTest()
    @Test
    fun testReferenceTypesNamed() = doTest()
    @Test
    fun testReturn() = doTest()
    @Test
    fun testReturnBlock() = doTest()
    @Test
    fun testReturnIf() = doTest()
    @Test
    fun testReturnVoid() = doTest()
    @Test
    fun testSelect() = doTest()
    @Test
    fun testSetglobalNamed() = doTest()
    @Test
    fun testSetglobal() = doTest()
    @Test
    fun testSetlocalIndexAfterParam() = doTest()
    @Test
    fun testSetlocalIndexMixedNamedUnnamed() = doTest()
    @Test
    fun testSetlocalNamed() = doTest()
    @Test
    fun testSetlocalParamNamed() = doTest()
    @Test
    fun testSetlocalParam() = doTest()
    @Test
    fun testSetlocal() = doTest()
    @Test
    fun testStoreAligned() = doTest()
    @Test
    fun testStoreOffset() = doTest()
    @Test
    fun testStore() = doTest()
    @Test
    fun testTableCopy() = doTest()
    @Test
    fun testTableDrop() = doTest()
    @Test
    fun testTableGet() = doTest()
    @Test
    fun testTableGrow() = doTest()
    @Test
    fun testTableInit() = doTest()
    @Test
    fun testTableSet() = doTest()
    @Test
    fun testTeeLocal() = doTest()
    @Test
    fun testUnaryExtend() = doTest()
    @Test
    fun testUnary() = doTest()
    @Test
    fun testUnreachable() = doTest()

    @Test
    fun testBadBrName() = doTest()
    @Test
    fun testBadBrNoDepth() = doTest()
    @Test
    fun testBadBrtableNoVars() = doTest()
    @Test
    fun testBadConstF32Trailing() = doTest()
    @Test
    fun testBadConstI32Garbage() = doTest()
    @Test
    fun testBadConstI32JustNegativeSign() = doTest()
    @Test
    fun testBadConstI32Trailing() = doTest()
    @Test
    fun testBadConstTypeI32InNonSimdConst() = doTest()
    @Test
    fun testBadConvertFloatSign() = doTest()
    @Test
    fun testBadConvertIntNoSign() = doTest()
    @Test
    fun testBadGetlocalName() = doTest()
    @Test
    fun testBadIfNoThen() = doTest()
    @Test
    fun testBadLoadAlign() = doTest()
    @Test
    fun testBadLoadAlignMisspelled() = doTest()
    @Test
    fun testBadLoadAlignNegative() = doTest()
    @Test
    fun testBadLoadFloatSign() = doTest()
    @Test
    fun testBadLoadOffsetNegative() = doTest()
    @Test
    fun testBadLoadType() = doTest()
    @Test
    fun testBadNop() = doTest()
    @Test
    fun testBadSetlocalName() = doTest()
    @Test
    fun testBadStoreAlign() = doTest()
    @Test
    fun testBadStoreFloatSign() = doTest()
    @Test
    fun testBadStoreOffsetNegative() = doTest()
    @Test
    fun testBadStoreType() = doTest()
    @Test
    fun testBadUnexpected() = doTest()
}