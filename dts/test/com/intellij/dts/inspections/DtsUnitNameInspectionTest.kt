package com.intellij.dts.inspections

class DtsUnitNameInspectionTest : DtsInspectionTest(DtsUnitNameInspection::class) {
    override fun getBasePath(): String = "inspections/unitName"

    fun `test leading 0s`() = doTest()
    fun `test leading 0x`() = doTest()
}