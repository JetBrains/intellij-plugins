// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.sfc

import com.intellij.lang.BracePair
import com.intellij.lang.javascript.highlighting.JSBraceMatcher
import com.intellij.psi.tree.IElementType
import org.jetbrains.astro.lang.sfc.highlighting.AstroFrontmatterHighlighterToken

class AstroSfcBraceMatcher : JSBraceMatcher() {

  override fun getPairs(): Array<BracePair> =
    PAIRS

  /**
   * Returns `true` if paired rbrace should be inserted after lbrace of given type when lbrace is encountered before contextType token.
   * It is safe to always return `true`, then paired brace will be inserted anyway.
   *
   * @param lbraceType  lbrace for which information is queried
   * @param contextType token type that follows lbrace
   * @return true / false as described
   */
  override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean =
    super.isPairedBracesAllowedBeforeType(AstroFrontmatterHighlighterToken.unwrap(lbraceType),
                                          contextType?.let { AstroFrontmatterHighlighterToken.unwrap(it) })

  companion object {
    private val PAIRS = JSBraceMatcher()
      .pairs
      .flatMap { sequenceOf(it, BracePair(AstroFrontmatterHighlighterToken[it.leftBraceType],
                                          AstroFrontmatterHighlighterToken[it.rightBraceType], it.isStructural))
      }
      .toTypedArray()
  }

}