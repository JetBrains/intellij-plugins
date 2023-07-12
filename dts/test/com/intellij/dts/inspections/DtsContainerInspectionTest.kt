package com.intellij.dts.inspections

class DtsContainerInspectionTest : DtsInspectionTest(DtsContainerInspection::class) {
    override fun getBasePath(): String = "inspections/container"
    
    override fun getTestFileExtension(): String = "dts"

    fun `test root property`() = doTest()
    fun `test root sub node`() = doTest()
    fun `test root delete property`() = doTest()
    fun `test root delete node by name`() = doTest()
    fun `test root delete node by ref`() = doTest()
    fun `test node root node`() = doTest()
    fun `test node v1`() = doTest()
    fun `test node delete node by name`() = doTest()
    fun `test node delete node by ref`() = doTest()
}