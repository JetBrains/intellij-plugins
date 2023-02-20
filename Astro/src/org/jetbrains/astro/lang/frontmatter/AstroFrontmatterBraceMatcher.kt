// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.frontmatter

import com.intellij.lang.BracePair
import com.intellij.lang.javascript.highlighting.JSBraceMatcher

class AstroFrontmatterBraceMatcher : JSBraceMatcher() {

  override fun getPairs(): Array<BracePair> =
    PAIRS

  companion object {
    private val PAIRS = JSBraceMatcher()
      .pairs
      .flatMap {
        sequenceOf(it, BracePair(it.leftBraceType, it.rightBraceType, it.isStructural))
      }
      .toTypedArray()
  }

}