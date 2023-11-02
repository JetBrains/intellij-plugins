package com.intellij.dts.inspections

class DtsPropertyTypeInspectionTest : DtsInspectionTest(DtsPropertyTypeInspection::class) {
    override fun getBasePath(): String = "inspections/propertyType"

    override fun setUp() {
        super.setUp()
        addZephyr()
    }

    fun `test default property`() = doTest()

    fun `test binding property`() = doTest()
}