package org.jetbrains.qodana.vcs

import com.intellij.openapi.util.NlsSafe

@NlsSafe
fun String.trimRevisionString(): String {
  return take(8)
}