// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

open class Angular181HtmlLexerTest : Angular17HtmlLexerTest() {

  override val templateSyntax: Angular2TemplateSyntax
    get() = Angular2TemplateSyntax.V_18_1

  fun testLetBlockInvalidId() {
    doTest("""
      @let 12foo = test(12); the end
    """.trimIndent())
  }

  fun testLetBlockInvalidGlued() {
    doTest("""
      @letfoo = test(12); the end
    """.trimIndent())
  }

  fun testLetBlockNoSemicolon() {
    doTest("""
      @let foo = test(12) the end
    """.trimIndent())
  }

  fun testLetBlockNoEquals() {
    doTest("""
      @let foo test(12); the end
    """.trimIndent())
  }

  fun testLetBlockEmptyValue() {
    doTest("""
      @let foo =; the end
    """.trimIndent())
  }

  fun testLetBlockString() {
    doTest("""
      @let foo = "foo" + test(12); the end
    """.trimIndent())
  }

  fun testLetBlockStringUnterminated() {
    doTest("""
      @let foo = "foo + test(12); 
      the end
    """.trimIndent())
  }

  fun testLetBlockStringMultiline() {
    doTest("""
      @let foo = "foo 
        bar
        check" + test(12); the end
    """.trimIndent())
  }

  fun testLetBlockStringEscapeEof() {
    doTest("""
      @let foo = "foo\""".trimIndent())
  }

  fun testLetBlockStrings() {
    doTest("""
      @let foo = "foo\";bar" + 'foo\';bar' + test(12); the end
    """.trimIndent())
  }

}
