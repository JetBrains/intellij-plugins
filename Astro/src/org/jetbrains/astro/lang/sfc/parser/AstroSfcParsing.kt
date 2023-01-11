// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.astro.lang.sfc.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.html.HtmlParsing
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.xml.XmlTokenType
import org.jetbrains.astro.lang.sfc.lexer.AstroSfcTokenTypes

class AstroSfcParsing(builder: PsiBuilder) : HtmlParsing(builder) {
  override fun hasCustomTopLevelContent(): Boolean {
    return CUSTOM_CONTENT.contains(token())
  }

  override fun hasCustomTagContent(): Boolean {
    return CUSTOM_CONTENT.contains(token())
  }

  override fun parseProlog() {
    while(token().let { it == XmlTokenType.XML_COMMENT_CHARACTERS
                        || it == AstroSfcTokenTypes.FRONTMATTER_SEPARATOR
                        || it == AstroSfcTokenTypes.FRONTMATTER_SCRIPT})
      advance()
    super.parseProlog()
  }

  override fun parseCustomTagContent(xmlText: PsiBuilder.Marker?): PsiBuilder.Marker? {
    var result = xmlText
    when (token()) {

    }
    return result
  }

  override fun parseCustomTopLevelContent(error: PsiBuilder.Marker?): PsiBuilder.Marker? {
    val result = flushError(error)
    terminateText(parseCustomTagContent(null))
    return result
  }

  override fun parseAttribute() {
    when(token()) {
      else -> {
        super.parseAttribute()
      }
    }
  }

  override fun parseAttributeValue() {
    when(token()) {
      else -> {
        super.parseAttributeValue()
      }
    }
  }

  companion object {
    private val CUSTOM_CONTENT = TokenSet.create()
  }
}