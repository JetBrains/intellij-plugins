// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.codeinsight

import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.common.waitUntilAssertSucceeds

internal class TfBackendCompletionTestCase : TfBaseCompletionTestCase() {

  fun testTerraformBlockAdvised() {
    doBasicCompletionTest("<caret>", "terraform")
    doBasicCompletionTest("t<caret>", "terraform")
  }

  fun testBackendAllowedInTerraform() {
    doBasicCompletionTest("terraform{<caret>}", "backend")
    doBasicCompletionTest("terraform{backend \"<caret>\" {}}", "s3")
    doBasicCompletionTest("terraform{backend <caret> {}}", "s3")
  }

  fun testPropertiesInBackend() {
    doBasicCompletionTest("terraform{backend \"s3\" {<caret>}}", "bucket", "key")
  }

  fun testImportBlockCompletion() {
    val textBefore = "im<caret>"
    val textAfter = """
      import {
        id = ""
        to = ""
      }""".trimIndent()

    myFixture.configureByText(fileName, textBefore)
    val variants = myFixture.completeBasic()
    assertNull(variants)
    timeoutRunBlocking {
      waitUntilAssertSucceeds("Cannot add required properties to the import block") {
        myFixture.checkResult(textAfter)
      }
    }
  }

  override fun runInDispatchThread(): Boolean {
    return false
  }

  fun testImportToCompletion() {
    doBasicCompletionTest("""
                           import {
                             id = "abcdef"
                             to = aws_instance.<caret>
                           }
                           
                           resource "aws_instance" "a" {}
                           resource "aws_instance" "b" {}
                            """, 2, "a", "b")
  }

}