// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.blocks

const val BLOCK_IF = "if"
const val BLOCK_ELSE_IF = "else if"
const val BLOCK_ELSE = "else"

object Angular2HtmlBlockUtils {

  private val WHITESPACES = Regex("[ \t]+")

  fun String.toCanonicalBlockName() =
    removePrefix("@").replace(WHITESPACES, " ")

}