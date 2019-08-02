// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html

import com.intellij.lang.javascript.dialects.JSLanguageLevel
import org.jetbrains.vuejs.lang.html.highlighting.VueHighlightingLexer

class VueHighlightingLexerTest : VueLexerTest() {

  fun testScriptES6() = doTest("""
    |<script lang="typescript">
    |(() => {})();
    |</script>
  """)

  fun testTemplateHtml() = doTest("""
    |<template>
    |  <h2>{{title}}</h2>
    |</template>
  """)

  override fun createLexer() = VueHighlightingLexer(JSLanguageLevel.ES6)
  override fun getDirPath() = "/contrib/vuejs/vuejs-tests/testData/html/highlightingLexer"
}
