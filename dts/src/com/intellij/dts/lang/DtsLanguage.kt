package com.intellij.dts.lang

import com.intellij.dts.DtsBundle
import com.intellij.lang.Language

object DtsLanguage : Language("DTS") {
  override fun getDisplayName(): String = DtsBundle.message("language.name")
}