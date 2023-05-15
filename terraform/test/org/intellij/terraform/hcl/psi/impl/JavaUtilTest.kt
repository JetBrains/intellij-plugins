// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.psi.impl

import com.intellij.openapi.util.TextRange
import org.assertj.core.api.BDDAssertions.then
import org.junit.Test
import java.util.*

class JavaUtilTest {
  @Test
  @Throws(Exception::class)
  fun testDoGetTextFragments() {
    doTestDoGetTextFragments("\"\"") // No fragments in empty string literal
    doTestDoGetTextFragments("\"x\"", 1 to 1 to "x")
    doTestDoGetTextFragments("\"word\"", 1 to 4 to "word")
    doTestDoGetTextFragments("\"a\\\"b\"", 1 to 1 to "a", 2 to 2 to "\"", 4 to 1 to "b")
    doTestDoGetTextFragments("\"\\\"\"", 1 to 2 to "\"")
    doTestDoGetTextFragments("\"a\\\"\"", 1 to 1 to "a", 2 to 2 to "\"")

    doTestDoGetTextFragments("\"\${}\"", 1 to 3 to "\${}")
    doTestDoGetTextFragments("\"\${\"\"}\"", 1 to 5 to "\${\"\"}")
    doTestDoGetTextFragments("\"a\${}b\"", 1 to 1 to "a", 2 to 3 to "\${}", 5 to 1 to "b")
    doTestDoGetTextFragments("\"\${}\${}\"", 1 to 3 to "\${}", 4 to 3 to "\${}")
    doTestDoGetTextFragments("\"\${}x\${}\"", 1 to 3 to "\${}", 4 to 1 to "x", 5 to 3 to "\${}")
    doTestDoGetTextFragments("\"\${baz}\"", 1 to 6 to "\${baz}")
    doTestDoGetTextFragments("\"\${\"baz\"}\"", 1 to 8 to "\${\"baz\"}")
    doTestDoGetTextFragments("\"\${\\\"baz\\\"}\"", 1 to 10 to "\${\\\"baz\\\"}")

    doTestDoGetTextFragments("\"a\$\${}b\"", 1 to 6 to "a\$\${}b")
    doTestDoGetTextFragments("\"a%%{}b\"", 1 to 6 to "a%%{}b")
    doTestDoGetTextFragments("\"a%{}b\"", 1 to 1 to "a", 2 to 3 to "%{}", 5 to 1 to "b")
  }

  @Test
  @Throws(Exception::class)
  fun testDoGetTextFragments_EscapeCodes() {
    doTestDoGetTextFragments("\"a\\tb\"", 1 to 1 to "a", 2 to 2 to "\t", 4 to 1 to "b")
    val expected = ArrayList<Pair<Pair<Int, Int>, String>>()
    listOf('"', '\\', '\b', '\u000C', '\n', '\r', '\t', '\u000B', '\u0007').forEachIndexed { i, c ->
      expected.add(i * 3 + 1 to 2 to c.toString())
      expected.add(i * 3 + 1 + 2 to 1 to " ")
    }
    doTestDoGetTextFragments("\"\\\" \\\\ \\b \\f \\n \\r \\t \\v \\a \"", *expected.toTypedArray())

  }

  @Test
  @Throws(Exception::class)
  fun testDoGetTextFragments_EscapeCodes_WithNumbers() {
    doTestDoGetTextFragments("\"\\120 \"", 1 to 4 to "\\120", 5 to 1 to " ")
    doTestDoGetTextFragments("\"\\X50 \"", 1 to 4 to "\\X50",  5 to 1 to " ")
    doTestDoGetTextFragments("\"\\u0050 \"", 1 to 6 to "\\u0050", 7 to 1 to " ")
    doTestDoGetTextFragments("\"\\U00000050 \"", 1 to 10 to "\\U00000050", 11 to 1 to " ")
  }

  @Test
  @Throws(Exception::class)
  fun testDoGetTextFragments_EscapeCodes_InsideInterpolations() {
    doTestDoGetTextFragments("\"\${\"\\n\\u0050\"} \"", 1 to 13 to "\${\"\\n\\u0050\"}", 14 to 1 to " ")
  }

  @Test
  @Throws(Exception::class)
  fun testDoGetTextFragments_ClosingCurlyBracesInStrings() {
    doTestDoGetTextFragments("\"\${x(\"}\", \"$\")}\"", 1 to 14 to "\${x(\"}\", \"$\")}")
    doTestDoGetTextFragments("\"\${x(\"}\")}\"", 1 to 9 to "\${x(\"}\")}")
  }

  private fun doTestDoGetTextFragments(text: String, vararg expected: Pair<Pair<Int, Int>, String>) {
    val rangesAndValues: List<Pair<TextRange, String>> = expected.map { TextRange.from(it.first.first, it.first.second) to it.second }
    val fragments: List<Pair<TextRange, String>> = JavaUtil.doGetTextFragments(text, true, true).map { it.first to it.second }
    then(fragments).isEqualTo(rangesAndValues)
  }
}