package com.intellij.dts.inspections

class DtsPropertyNameInspectionTest : DtsInspectionTest(DtsPropertyNameInspection::class) {
    override fun getBasePath(): String = "propertyName"

    fun testInvalid() = doTestHighlighting()
}