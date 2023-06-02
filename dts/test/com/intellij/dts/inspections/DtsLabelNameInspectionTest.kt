package com.intellij.dts.inspections

class DtsLabelNameInspectionTest : DtsInspectionTest(DtsLabelNameInspection::class) {
    override fun getBasePath(): String = "labelName"

    fun testInvalidStart() = doTestHighlighting()

    fun testInvalid() = doTestHighlighting()
}