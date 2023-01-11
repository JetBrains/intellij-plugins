// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.sfc.lexer

import com.intellij.html.embedding.HtmlEmbeddedContentProvider
import com.intellij.html.embedding.HtmlEmbeddedContentSupport
import com.intellij.html.embedding.HtmlTokenEmbeddedContentProvider
import com.intellij.lexer.BaseHtmlLexer
import org.jetbrains.astro.lang.sfc.highlighting.AstroSfcHighlightingLexer
import org.jetbrains.astro.lang.typescript.AstroFrontmatterHighlightingLexer

class AstroSfcEmbeddedContentSupport : HtmlEmbeddedContentSupport {

  override fun isEnabled(lexer: BaseHtmlLexer): Boolean {
    return lexer is AstroSfcLexer || lexer is AstroSfcHighlightingLexer
  }

  override fun createEmbeddedContentProviders(lexer: BaseHtmlLexer): List<HtmlEmbeddedContentProvider> {
    return listOf(
      HtmlTokenEmbeddedContentProvider(
        lexer,
        AstroSfcTokenTypes.FRONTMATTER_SCRIPT,
        { AstroFrontmatterHighlightingLexer() }
      )
    )
  }

}