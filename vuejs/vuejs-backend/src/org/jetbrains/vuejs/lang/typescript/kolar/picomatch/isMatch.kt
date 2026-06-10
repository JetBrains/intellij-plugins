// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.picomatch

fun isMatch(
  str: String,
  pattern: String, // glob
): Boolean {
  if (!pattern.contains('*')) return str == pattern
  val regexStr = buildString {
    for (c in pattern) {
      when (c) {
        '*' -> append(".*")
        else -> append(Regex.escape(c.toString()))
      }
    }
  }
  return Regex(regexStr).matches(str)
}
