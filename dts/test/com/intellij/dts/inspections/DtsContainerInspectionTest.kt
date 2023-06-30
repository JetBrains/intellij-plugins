package com.intellij.dts.inspections

class DtsContainerInspectionTest : DtsInspectionTest(DtsContainerInspection::class) {
    override fun getBasePath(): String = "container"

    fun testRootProperty() = doTestHighlighting(extension = "dts")

    fun testRootSubNode() = doTestHighlighting(extension = "dts")

    fun testRootDeleteProperty() = doTestHighlighting(extension = "dts")

    fun testRootDeleteNodeByName() = doTestHighlighting(extension = "dts")

    fun testRootDeleteNodeByRef() = doTestHighlighting(extension = "dts")

    fun testNodeRootNode() = doTestHighlighting(extension = "dts")

    fun testNodeV1() = doTestHighlighting(extension = "dts")

    fun testNodeDeleteNodeByName() = doTestHighlighting(extension = "dts")

    fun testNodeDeleteNodeByRef() = doTestHighlighting(extension = "dts")
}