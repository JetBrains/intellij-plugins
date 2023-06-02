package com.intellij.dts.inspections

class DtsUnitNameInspectionTest : DtsInspectionTest(DtsUnitNameInspection::class) {
    override fun getBasePath(): String = "unitName"

    fun testLeading0s() = doTestHighlighting()

    fun testLeading0x() = doTestHighlighting()
}