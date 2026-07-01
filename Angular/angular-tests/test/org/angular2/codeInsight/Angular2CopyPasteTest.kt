// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.javascript.testFramework.web.configureAndCopyPaste
import com.intellij.javascript.testFramework.web.performCopyPaste
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.SkipTsGoProxy
import org.angular2.TestNoService
import org.angular2.TestTsGoProxy
import org.junit.Test

@TestNoService
@TestTsGoProxy
class Angular2CopyPasteTest : Angular2TestCase("copyPaste") {

  @Test
  fun testBasic() {
    doTest("html", "html")
  }

  @Test
  @SkipTsGoProxy // runs forever
  fun testBasicToInjected() {
    doTest("html", "ts")
  }

  @Test
  fun testInjected() {
    doTest("ts", "ts")
  }

  @Test
  fun testInjectedToBasic() {
    doTest("ts", "html")
  }

  @Test
  fun testExpression() {
    doTest("html", "html")
  }

  @Test
  fun testExpressionFromInjected() {
    doTest("ts", "html")
  }

  @Test
  fun testExpressionToInjected() {
    doTest("html", "ts")
  }

  @Test
  fun testExpressionToHtml() {
    doTest("html", "html")
  }

  @Test
  fun testHtmlToExpression() {
    doTest("html", "html")
  }

  @Test
  fun testNgFor() {
    doTest("html", "html")
  }

  @Test
  fun testSelf() {
    doSameFileTest("html", "</div >\n<caret>")
  }

  @Test
  fun testUndefined() {
    doTest("html", "html")
  }

  @Test
  fun testAliased() {
    doTest("html", "html")
  }

  private fun doSameFileTest(ext: String, pasteSignature: String) {
    doConfiguredTest(Angular2TestModule.ANGULAR_CORE_13_3_5,
                     Angular2TestModule.ANGULAR_COMMON_13_3_5,
                     Angular2TestModule.ANGULAR_CDK_14_2_0,
                     configureFile = true, dir = true,
                     configureFileName = "source.component.$ext", checkResult = true) {
      performCopyPaste(pasteSignature)
    }
  }

  private fun doTest(srcExt: String, destExt: String) {
    doConfiguredTest(Angular2TestModule.ANGULAR_CORE_13_3_5,
                     Angular2TestModule.ANGULAR_COMMON_13_3_5,
                     Angular2TestModule.ANGULAR_CDK_14_2_0,
                     configureFile = false, dir = true, checkResult = true) {
      configureAndCopyPaste("source.component.$srcExt", "destination.component.$destExt")
    }
  }

}