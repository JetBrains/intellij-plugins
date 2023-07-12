package com.intellij.dts.inspections

class DtsStatementOrderInspectionTest : DtsInspectionTest(DtsStatementOrderInspection::class) {
    override fun getBasePath(): String = "inspections/statementOrder"

    fun `test invalid property`() = doTest()
    fun `test invalid delete property`() = doTest()
    fun `test after delete node`() = doTest()
}