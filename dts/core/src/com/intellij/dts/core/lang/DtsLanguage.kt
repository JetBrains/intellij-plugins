package com.intellij.dts.core.lang

import com.intellij.dts.core.DtsBundle
import com.intellij.lang.Language

object DtsLanguage : Language("DTS") {
  override fun getDisplayName(): String = DtsBundle.message("language.name")
}