// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.codeInsight

import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.hints.JSComponentUsageProvider
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.elementType
import com.intellij.psi.xml.XmlElementType
import com.intellij.psi.xml.XmlTokenType
import org.jetbrains.astro.lang.AstroFileImpl

private val SKIP_TOKENS = TokenSet.orSet(
  JSTokenTypes.COMMENTS_AND_WHITESPACES,
  XmlTokenType.COMMENTS,
  XmlTokenType.WHITESPACES,
  TokenSet.create(XmlElementType.XML_COMMENT),
)

class AstroComponentUsageProvider : JSComponentUsageProvider {
  override fun computeRangeForInlineHint(file: PsiFile, document: Document): TextRange? {
    if (file is AstroFileImpl) {
      var anchor = file.astroContentRoot()?.firstChild
      while (anchor != null) {
        if (anchor.elementType !in SKIP_TOKENS) {
          return anchor.textRange
        }
        anchor = anchor.nextSibling
      }
    }

    return super.computeRangeForInlineHint(file, document)
  }
}