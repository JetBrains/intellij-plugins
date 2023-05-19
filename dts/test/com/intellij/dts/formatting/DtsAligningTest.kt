package com.intellij.dts.formatting

class DtsAligningTest : DtsFormattingTest() {
    override fun getBasePath(): String = "aligning"

    fun testPropertyAssignments() = doFormattingTest(alignPropertyAssignment = true)

    fun testPropertyValues() = doFormattingTest(alignPropertyValues = true, keepLineBreaks = true)

    fun testPropertyAssignmentsAndValues() = doFormattingTest(
        alignPropertyValues = true,
        alignPropertyAssignment = true,
        keepLineBreaks = true,
    )
}