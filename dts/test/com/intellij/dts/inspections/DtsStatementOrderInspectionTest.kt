package com.intellij.dts.inspections

class DtsStatementOrderInspectionTest : DtsInspectionTest(DtsStatementOrderInspection::class) {
    override fun getBasePath(): String = "statementOrder"

    fun testInvalidProperty() = doTestHighlighting()

    fun testInvalidDeleteProperty() = doTestHighlighting()

    fun testAfterDeleteNode() = doTestHighlighting()
}