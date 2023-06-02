package com.intellij.dts.inspections

class DtsNodeNameInspectionTest : DtsInspectionTest(DtsNodeNameInspection::class) {
    override fun getBasePath(): String = "nodeName"

    fun testInvalid() = doTestHighlighting()

    fun testMultipleAt() = doTestHighlighting()
}