package com.intellij.dts.inspections

class DtsPropertyNameInspectionTest : DtsInspectionTest(DtsPropertyNameInspection::class) {
    override fun getBasePath(): String = "inspections/propertyName"

    fun `test invalid`() = doTest()
    fun `test valid`() = doTest()
}