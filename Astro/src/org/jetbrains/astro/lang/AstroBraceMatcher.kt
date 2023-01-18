// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang

import com.intellij.lang.BracePair
import com.intellij.lang.javascript.highlighting.JSBraceMatcher
import com.intellij.psi.tree.IElementType
import org.jetbrains.astro.lang.highlighting.AstroFrontmatterHighlighterToken

class AstroBraceMatcher : JSBraceMatcher() {

  override fun getPairs(): Array<BracePair> =
    PAIRS

  override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean =
    super.isPairedBracesAllowedBeforeType(AstroFrontmatterHighlighterToken.unwrap(lbraceType),
                                          contextType?.let { AstroFrontmatterHighlighterToken.unwrap(it) })

  companion object {
    private val PAIRS = JSBraceMatcher()
      .pairs
      .flatMap {
        sequenceOf(it, BracePair(AstroFrontmatterHighlighterToken[it.leftBraceType],
                                 AstroFrontmatterHighlighterToken[it.rightBraceType],
                                 it.isStructural))
      }
      .toTypedArray()
  }

}