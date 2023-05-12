package org.jetbrains.webstorm.lang

import com.intellij.lang.Language

object WebAssemblyLanguage : Language("WebAssembly") {
    override fun isCaseSensitive() = true
}