// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.html.HtmlParsing

class VueParsing(builder: PsiBuilder): HtmlParsing(builder) {
  override fun isSingleTag(tagName: String, originalTagName: String): Boolean {
    // There are heavily-used Vue components called like 'Col' or 'Input'. Unlike HTML tags <col> and <input> Vue components do have closing tags.
    // The following 'if' is a little bit hacky but it's rather tricky to solve the problem in a better way at parser level.
    if (tagName != originalTagName) {
      return false
    }
    return super.isSingleTag(tagName, originalTagName)
  }
}
