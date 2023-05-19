package com.intellij.dts.formatting

import com.intellij.dts.lang.DtsLanguage
import com.intellij.dts.settings.DtsCodeStyleSettings
import com.intellij.psi.formatter.FormatterTestCase

abstract class DtsFormattingTest : FormatterTestCase() {
    override fun getTestDataPath() = "testData/formatting"

    override fun getFileExtension() = "dtsi"

    override fun getTestName(lowercaseFirstLetter: Boolean): String = super.getTestName(false)

    protected fun doFormattingTest(
        keepLineBreaks: Boolean = false,
        alignPropertyAssignment: Boolean = false,
        alignPropertyValues: Boolean = false,
    ) {
        val common = settings.getCommonSettings(DtsLanguage)
        common.KEEP_LINE_BREAKS = keepLineBreaks

        val custom = settings.getCustomSettings(DtsCodeStyleSettings::class.java)
        custom.ALIGN_PROPERTY_ASSIGNMENT = alignPropertyAssignment
        custom.ALIGN_PROPERTY_VALUES = alignPropertyValues

        doTest()
    }
}