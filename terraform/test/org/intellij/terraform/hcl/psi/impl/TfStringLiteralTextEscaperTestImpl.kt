// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.psi.impl

import com.intellij.openapi.util.TextRange
import org.intellij.terraform.config.psi.TfElementGenerator
import org.intellij.terraform.hcl.psi.HCLElementGenerator

class TfStringLiteralTextEscaperTestImpl : HCLStringLiteralTextEscaperTest() {
  override fun createElementGenerator(): HCLElementGenerator {
    return TfElementGenerator(getProject())
  }

  fun testRelevantRangeTF() {
    doTestRelevantRange("\"\${}\"", TextRange.from(1, 3))
  }

  fun testDecodeSuccessfullyTF() {
    doTestDecode("\"\${}\"", TextRange.from(1, 3), "\${}", 0, 1, 2, 3)
    doTestDecode("\"\${\"\"}\"", TextRange.from(1, 5), "\${\"\"}", 0, 1, 2, 3, 4, 5)
    doTestDecode("\"\${\\\"}\"", TextRange.from(1, 5), "\${\\\"}", 0, 1, 2, 3, 4, 5) // "${\"}" -> ${\"}
    doTestDecode("\"\${\\\\}\"", TextRange.from(1, 5), "\${\\\\}", 0, 1, 2, 3, 4, 5) // "${\\}" -> ${\\}
    doTestDecode("\"\${\\\\\"}\"", TextRange.from(1, 6), "\${\\\\\"}", 0, 1, 2, 3, 4, 5, 6) // "${\\"}" -> ${\\"}
    doTestDecode("\"\${\"\\\\\",\\\\\"\"}\"", TextRange.from(1, 12), "\${\"\\\\\",\\\\\"\"}", 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12) // "${"\\",\\""}"  -> ${"\\",\\""}
  }

  fun testOffsetInHostTF() {
    doTestOffsetInHost("\"\${}\"", 1, 2, 3, 4)
    doTestOffsetInHost("\"\${\"\"}\"", 1, 2, 3, 4, 5, 6) // "${""}" -> ${""}
    doTestOffsetInHost("\"\${\\\"}\"", 1, 2, 3, 4, 5, 6) // "${\"}" -> ${\"}
    doTestOffsetInHost("\"\${\\\\}\"", 1, 2, 3, 4, 5, 6) // "${\\}" -> ${\\}
    doTestOffsetInHost("\"\${\"\\\\\",\\\\\"\"}\"", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13) // "${"\\",\\""}" -> ${"\\",\\""}
  }
}