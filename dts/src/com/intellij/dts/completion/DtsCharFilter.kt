package com.intellij.dts.completion

import com.intellij.codeInsight.lookup.CharFilter
import com.intellij.codeInsight.lookup.Lookup
import com.intellij.dts.lang.DtsFile

private val rx = Regex("[a-zA-Z0-9,._+*#?@-]")

class DtsCharFilter : CharFilter() {
  override fun acceptChar(c: Char, prefixLength: Int, lookup: Lookup): Result? {
    if (lookup.psiFile !is DtsFile) return null

    if (rx.matches(c.toString())) {
      return Result.ADD_TO_PREFIX
    }
    else {
      return Result.HIDE_LOOKUP
    }
  }
}
