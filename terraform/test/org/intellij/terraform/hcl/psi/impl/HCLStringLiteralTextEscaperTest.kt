// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.psi.impl

import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.LightPlatformTestCase
import org.assertj.core.api.BDDAssertions.then
import org.intellij.terraform.hcl.psi.HCLElementGenerator
import org.junit.Assert
import org.junit.Test

open class HCLStringLiteralTextEscaperTest : LightPlatformTestCase() {
  protected lateinit var myElementGenerator: HCLElementGenerator

  @Throws(Exception::class)
  public override fun setUp() {
    super.setUp()
    myElementGenerator = createElementGenerator()
  }

  protected open fun createElementGenerator(): HCLElementGenerator {
    return HCLElementGenerator(getProject())
  }

  @Test
  open fun testRelevantRange() {
    doTestRelevantRange("\"\"", TextRange.from(1, 0))
    doTestRelevantRange("\"x\"", TextRange.from(1, 1))
    doTestRelevantRange("\"aba\"", TextRange.from(1, 3))
    doTestRelevantRange("\"a\\\"b\"", TextRange.from(1, 4))
    doTestRelevantRange("\"a\\\\\"", TextRange.from(1, 3))
  }

  @Test
  open fun testDecodeSuccessfully() {
    doTestDecode("\"\"", TextRange.from(1, 0), "", 0)
    doTestDecode("\"ab\"", TextRange.from(1, 2), "ab", 0, 1, 2)
    doTestDecode("\"\\\"\"", TextRange.from(1, 2), "\"", 0, 2, 0)
    doTestDecode("\"\\\\\"", TextRange.from(1, 2), "\\", 0, 2, 0)
  }

  @Test
  open fun testOffsetInHost() {
    doTestOffsetInHost("\"ab\"", 1, 2, 3)
    doTestOffsetInHost("\"\\\"\"", 1, 3)
    doTestOffsetInHost("\"\\t\"", 1, 3)
    doTestOffsetInHost("\"\"", 1)
    doTestOffsetInHost("\"\\\\b\"", 1, 3, 4)
    doTestOffsetInHost("\"\\\\\"", 1, 3)
  }

  protected fun doTestDecode(text: String, range: TextRange, expected: String, vararg offsets: Int) {
    val literal = myElementGenerator.createValue<HCLStringLiteralMixin>(text)
    val escaper = literal.createLiteralTextEscaper() as HCLStringLiteralTextEscaper
    val relevantTextRange = escaper.relevantTextRange
    Assert.assertTrue("Range $range should lay inside relevant range $relevantTextRange", range.intersects(relevantTextRange))
    val out = StringBuilder()
    val decode = escaper.decode(range, out)
    Assert.assertTrue("Successfully decoded", decode)
    then(getSourceOffsets(escaper)).isEqualTo(offsets)
    then(out.toString()).isEqualTo(expected)
  }

  protected fun doTestOffsetInHost(text: String, vararg offsets: Int) {
    val literal = myElementGenerator.createValue<HCLStringLiteralMixin>(text)
    val escaper = literal.createLiteralTextEscaper() as HCLStringLiteralTextEscaper
    val range = escaper.relevantTextRange
    val out = StringBuilder()
    val decode = escaper.decode(range, out)
    Assert.assertTrue("Successfully decoded", decode)

    val calculated: List<Int> = (0..out.length).map { escaper.getOffsetInHost(it, range) }
    then(calculated).isEqualTo(offsets.toList())
  }

  protected fun doTestRelevantRange(text: String, expected: TextRange) {
    val literal = myElementGenerator.createValue<HCLStringLiteralMixin>(text)
    val escaper = literal.createLiteralTextEscaper() as HCLStringLiteralTextEscaper
    then(escaper.relevantTextRange).isEqualTo(expected)
  }

  private fun getSourceOffsets(escaper: HCLStringLiteralTextEscaper): IntArray? {
    val field = HCLStringLiteralTextEscaper::class.java.getDeclaredField("outSourceOffsets")
    field.isAccessible = true
    val value = field.get(escaper)
    return value as IntArray?
  }
}