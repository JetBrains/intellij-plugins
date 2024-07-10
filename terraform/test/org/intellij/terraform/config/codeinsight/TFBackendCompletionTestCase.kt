// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.codeinsight

internal class TFBackendCompletionTestCase: TFBaseCompletionTestCase() {

  fun testTerraformBlockAdvised() {
    doBasicCompletionTest("<caret>", "terraform")
    doBasicCompletionTest("t<caret>", "terraform")
  }

  fun testBackendAllowedInTerraform() {
    doBasicCompletionTest("terraform{<caret>}", "backend")
    doBasicCompletionTest("terraform{backend \"<caret>\" {}}", "s3")
    doBasicCompletionTest("terraform{backend <caret> {}}", "s3")

    //    TODO: Investigate and uncomment. For now it's ok since autocompletion handler on 'backend' would add name and braces
    //    doBasicCompletionTest("terraform{backend \"<caret>\"}", "s3")
    //    doBasicCompletionTest("terraform{backend <caret>}", "s3")
  }

  fun testPropertiesInBackend() {
    doBasicCompletionTest("terraform{backend \"s3\" {<caret>}}", "bucket", "key")
  }

  fun _testImportBlockCompletion() {
    val textBefore = "im<caret>"
    val textAfter = """import {
                         id = ""
                         to = ""
                       }""".trim()

    myFixture.configureByText(fileName, textBefore)
    val variants = myFixture.completeBasic()
    assertNull(variants)
    myFixture.checkResult(textAfter)
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