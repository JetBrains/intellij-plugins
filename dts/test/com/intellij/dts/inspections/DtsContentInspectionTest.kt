package com.intellij.dts.inspections

class DtsContentInspectionTest : DtsInspectionTest(DtsContentInspection::class) {
    override fun getBasePath(): String = "content"

    fun testRootProperty() = doTestHighlighting(extension = "dts")

    fun testRootSubNode() = doTestHighlighting(extension = "dts")

    fun testRootDeleteProperty() = doTestHighlighting(extension = "dts")

    fun testRootDeleteNodeByName() = doTestHighlighting(extension = "dts")

    fun testRootDeleteNodeByRef() = doTestHighlighting(extension = "dts")
}