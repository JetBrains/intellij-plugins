package com.intellij.dts.settings

import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CustomCodeStyleSettings

class DtsCodeStyleSettings(container: CodeStyleSettings) : CustomCodeStyleSettings(DtsCodeStyleSettings::class.java.simpleName, container) {
    @JvmField
    var SPACE_WITHIN_ANGULAR_BRACKETS = false
    @JvmField
    var SPACE_WITHIN_EMPTY_NODE = true
    @JvmField
    var SPACE_BETWEEN_BYTES = true
    @JvmField
    var SPACE_AFTER_LABEL = true

    @JvmField
    var ALIGN_PROPERTY_ASSIGNMENT = false
    @JvmField
    var ALIGN_PROPERTY_VALUES = true

    @JvmField
    var MAX_BLANK_LINES_AROUND_PROPERTY = 1
    @JvmField
    var MIN_BLANK_LINES_AROUND_PROPERTY = 0
    @JvmField
    var MAX_BLANK_LINES_AROUND_NODE = 1
    @JvmField
    var MIN_BLANK_LINES_AROUND_NODE = 1
}