// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.codeinsight

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.terraform.TfTestUtils

internal class HCLLiteralAnnotatorTest : BasePlatformTestCase() {
  override fun setUp() {
    super.setUp()
    myFixture.setTestDataPath(basePath)
  }

  override fun getBasePath(): String {
    return TfTestUtils.getTestDataPath() + "/terraform/annotator/"
  }

  fun testNumbers() {
    doTestHighlighting()
  }

  fun testMethodCalls() {
    doTestHighlighting()
  }

  fun testForEllipsis() {
    doTestHighlighting()
  }

  fun testHCL1StringKeys() {
    doTestHighlighting(isTfFile = true)
  }

  fun test324() {
    doTestHighlighting(isTfFile = true)
  }

  fun testProviderFunction() {
    doTestHighlighting(isTfFile = true)
  }

  private fun doTestHighlighting(
    checkInfo: Boolean = false,
    checkWeakWarning: Boolean = true,
    checkWarning: Boolean = true,
    isTfFile: Boolean = false,
  ) {
    myFixture.testHighlighting(checkWarning, checkInfo, checkWeakWarning, getTestName(false) + "." + if (isTfFile) "tf" else "hcl")
  }
}
