// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.psi.impl

import org.junit.Assert
import org.junit.Test

class HCLQuoterTest {
  @Test
  fun testUnquote() {
    doUnquoteTest("\"\"", "")
    doUnquoteTest("\"a\"", "a")
    doUnquoteTest("\"\\120\"", "P")
    doUnquoteTest("\"\\X50\"", "P")
    doUnquoteTest("\"\\u0050\"", "P")
    doUnquoteTest("\"\\U00000050\"", "P")
    doUnquoteTest("\"\\\"x\\\"\"", "\"x\"")

    doUnquoteTest("\"\${join(\"\\\\\",\\\\\"\", values(var.developers))}\"", "\${join(\"\\\",\\\"\", values(var.developers))}")
  }

  @Test
  fun testUnquoteSafely() {
    doUnquoteTest("\"\"", "", true)
    doUnquoteTest("\"a\"", "a", true)
    doUnquoteTest("\"\\129\"", "\\129", true)
    doUnquoteTest("\"\\XR0\"", "\\XR0", true)
    doUnquoteTest("\"\\u050\"", "\\u050", true)
    doUnquoteTest("\"\\uR50\"", "\\uR50", true)
    doUnquoteTest("\"\\U00050\"", "\\U00050", true)
    doUnquoteTest("\"\\U00R00050\"", "\\U00R00050", true)
    doUnquoteTest("\"\\'\"", "\\'", true)
    doUnquoteTest("\"\\\"\"", "\"", true)
  }

  @Test
  fun testFailedUnquote() {
    doFailedUnquoteTest("\"\\129\"")
    doFailedUnquoteTest("\"\\XR0\"")
    doFailedUnquoteTest("\"\\uR050\"")
    doFailedUnquoteTest("\"\\u050\"")
    doFailedUnquoteTest("\"\\U00R00050\"")
    doFailedUnquoteTest("\"\\U00050\"")
    doFailedUnquoteTest("\"\\'\"")
  }

  @Test
  fun testEscape() {
    doEscapeTest("", "")
    doEscapeTest("aba", "aba")
    doEscapeTest("a\nb", "a\\nb")
    doEscapeTest("a\tb", "a\\tb")
    doEscapeTest("a\rb", "a\\rb")
    doEscapeTest("a\"b", "a\\\"b")
    doEscapeTest("a\uE000b", "a\\uE000b")
    doEscapeTest("a\\nb", "a\\\\nb")
    doEscapeTest("a/b", "a/b")
  }

  @Test
  fun testEscape_Interpolation() {
    doEscapeTest("\${}", "\${}")
    doEscapeTest("\${baz}", "\${baz}")
    doEscapeTest("\$\${baz}", "\$\${baz}")
    doEscapeTest("\${\"\"}", "\${\"\"}")
    doEscapeTest(" %{\\\"}", " %{\\\\\"}")
    doEscapeTest("\${\\\"}", "\${\\\\\"}")
    doEscapeTest("{\\\"}", "{\\\\\\\"}")
    doEscapeTest("\$\${\\\"}", "\$\${\\\\\\\"}")
    doEscapeTest("\${\"a/b\"}", "\${\"a/b\"}")
  }

  private fun doUnquoteTest(text: String, expected: String, safe: Boolean = false) {
    val unquote = HCLQuoter.unquote(text, safe = safe)
    Assert.assertEquals(expected, unquote)
  }

  private fun doEscapeTest(text: String, expected: String) {
    val unquote = HCLQuoter.escape(text)
    Assert.assertEquals(expected, unquote)
  }

  private fun doFailedUnquoteTest(text: String) {
    try {
      HCLQuoter.unquote(text)
      Assert.fail("Exception expected for input $text")
    } catch(e: Exception) {
    }
  }
}