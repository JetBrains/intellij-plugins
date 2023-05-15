package com.intellij.dts.lang

import com.intellij.lang.Language

object DtsLanguage : Language("DTS") {
    override fun getDisplayName(): String {
        return "Device Tree"
    }
}