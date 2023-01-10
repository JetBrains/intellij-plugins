// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.astro.lang.sfc.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.html.HTMLParser
import com.intellij.lang.html.HtmlParsing

class AstroSfcParser : HTMLParser() {
  override fun createHtmlParsing(builder: PsiBuilder): HtmlParsing {
    return AstroSfcParsing(builder)
  }
}