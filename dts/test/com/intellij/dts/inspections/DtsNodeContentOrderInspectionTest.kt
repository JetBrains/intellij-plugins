package com.intellij.dts.inspections

class DtsNodeContentOrderInspectionTest : DtsInspectionTest(DtsNodeContentOrderInspection::class) {
    override fun getBasePath(): String = "nodeContentOrder"

    fun testInvalidProperty() = doTestHighlighting()

    fun testInvalidDeleteProperty() = doTestHighlighting()

    fun testAfterDeleteNode() = doTestHighlighting()
}