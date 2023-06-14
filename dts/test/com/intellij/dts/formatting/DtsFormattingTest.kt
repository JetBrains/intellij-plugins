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
        maxBlankLinesAroundProperty: Int = 1,
        minBlankLinesAroundProperty: Int = 0,
        maxBlankLinesAroundNode: Int = 1,
        minBlankLinesAroundNode: Int = 1
    ) {
        val common = settings.getCommonSettings(DtsLanguage)
        common.KEEP_LINE_BREAKS = keepLineBreaks

        val custom = settings.getCustomSettings(DtsCodeStyleSettings::class.java)
        custom.ALIGN_PROPERTY_ASSIGNMENT = alignPropertyAssignment
        custom.ALIGN_PROPERTY_VALUES = alignPropertyValues
        custom.MAX_BLANK_LINES_AROUND_PROPERTY = maxBlankLinesAroundProperty
        custom.MIN_BLANK_LINES_AROUND_PROPERTY = minBlankLinesAroundProperty
        custom.MAX_BLANK_LINES_AROUND_NODE = maxBlankLinesAroundNode
        custom.MIN_BLANK_LINES_AROUND_NODE = minBlankLinesAroundNode

        doTest()
    }
}