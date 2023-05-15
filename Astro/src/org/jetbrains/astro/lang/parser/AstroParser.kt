// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.html.HTMLParser
import com.intellij.lang.html.HtmlParsing

class AstroParser : HTMLParser() {
  override fun createHtmlParsing(builder: PsiBuilder): HtmlParsing {
    return AstroParsing(builder)
  }
}