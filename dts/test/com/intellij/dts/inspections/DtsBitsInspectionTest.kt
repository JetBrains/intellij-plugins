package com.intellij.dts.inspections

class DtsBitsInspectionTest : DtsInspectionTest(DtsBitsInspection::class) {
    override fun getBasePath(): String = "bits"

    fun testValid() = doTestHighlighting()

    fun testInvalid() = doTestHighlighting()
}