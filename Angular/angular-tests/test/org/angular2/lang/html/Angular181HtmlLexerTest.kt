// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

import org.junit.jupiter.api.Test

open class Angular181HtmlLexerTest : Angular17HtmlLexerTest() {

  override val templateSyntax: Angular2TemplateSyntax
    get() = Angular2TemplateSyntax.V_18_1

  @Test
  fun testLetBlockInvalidId() {
    doTest("""
      @let 12foo = test(12); the end
    """.trimIndent())
  }

  @Test
  fun testLetBlockInvalidGlued() {
    doTest("""
      @letfoo = test(12); the end
    """.trimIndent())
  }

  @Test
  fun testLetBlockNoSemicolon() {
    doTest("""
      @let foo = test(12) the end
    """.trimIndent())
  }

  @Test
  fun testLetBlockNoEquals() {
    doTest("""
      @let foo test(12); the end
    """.trimIndent())
  }

  @Test
  fun testLetBlockEmptyValue() {
    doTest("""
      @let foo =; the end
    """.trimIndent())
  }

  @Test
  fun testLetBlockString() {
    doTest("""
      @let foo = "foo" + test(12); the end
    """.trimIndent())
  }

  @Test
  fun testLetBlockStringUnterminated() {
    doTest("""
      @let foo = "foo + test(12); 
      the end
    """.trimIndent())
  }

  @Test
  fun testLetBlockStringMultiline() {
    doTest("""
      @let foo = "foo 
        bar
        check" + test(12); the end
    """.trimIndent())
  }

  @Test
  fun testLetBlockStringEscapeEof() {
    doTest("""
      @let foo = "foo\""".trimIndent())
  }

  @Test
  fun testLetBlockStrings() {
    doTest("""
      @let foo = "foo\";bar" + 'foo\';bar' + test(12); the end
    """.trimIndent())
  }

}
