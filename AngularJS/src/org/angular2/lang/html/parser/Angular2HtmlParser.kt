// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.html.HTMLParser
import com.intellij.lang.html.HtmlParsing

class Angular2HtmlParser : HTMLParser() {
  override fun createHtmlParsing(builder: PsiBuilder): HtmlParsing {
    return Angular2HtmlParsing(builder)
  }
}