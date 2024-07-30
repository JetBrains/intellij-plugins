// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.terraform.hcl.psi.HCLPsiUtil.stripQuotes

class HCLPsiUtilTest : BasePlatformTestCase() {
  fun testStripQuotes() {
    doTestStripQuotes("'a'", "a")
    doTestStripQuotes("\"b\"", "b")
    doTestStripQuotes("\"\\\"c\\\"\"", "\\\"c\\\"")
    doTestStripQuotes("'\"d\"'", "\"d\"")
    doTestStripQuotes("'\${\"e\"}'", "\${\"e\"}")
  }

  fun testStripQuotesUnfinished() {
    doTestStripQuotes("'", "")
    doTestStripQuotes("\"", "")
    doTestStripQuotes("\\\"", "\\\"")
    doTestStripQuotes("'a", "a")
    doTestStripQuotes("\"b", "b")
    doTestStripQuotes("\"\\\"c\\\"", "\\\"c\\\"")
    doTestStripQuotes("'\\\"d\\\"", "\\\"d\\\"")
    doTestStripQuotes("\"'e'", "'e'")
    doTestStripQuotes("'\${\"f\"}", "\${\"f\"}")
  }

  fun testGetIdentifierPsi() {
    val psiFile = myFixture.configureByText("main.tf", """
      variable "instance_type" {
        description = "Type of EC2 instance"
        type        = string
      }
            
      data "aws_ami" "ubuntu" {
        most_recent = true
        owners      = ["099720109477"] # Canonical
      }
            
      locals {
        instance_name = "example-instance"
        tags = merge(var.instance_type, {
          "Name" = local.instance_name
        })
      }
      """.trimIndent())

    val blocks = psiFile.children.mapNotNull { it as? HCLBlock }
    checkIdentifierPsi("instance_type", blocks.first())
    checkIdentifierPsi("ubuntu", blocks[1])

    val hclObject = blocks.last().lastChild
    val locals = hclObject.children.mapNotNull { it as? HCLProperty }
    checkIdentifierPsi("instance_name", locals.first())
    checkIdentifierPsi("tags", locals.last())
  }

  private fun doTestStripQuotes(input: String, expected: String) {
    assertEquals(expected, stripQuotes(input))
  }

  private fun checkIdentifierPsi(expected: String, actual: HCLElement) {
    assertEquals(expected, HCLPsiUtil.getIdentifierPsi(actual)?.text?.trim('"'))
  }
}
