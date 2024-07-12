// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.editor

import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class HclCommenterTest : BasePlatformTestCase() {
  fun testLineHashComment() {
    myFixture.configureByText("main.tf", """
      resource "aws_instance" "example" {
        ami           = "ami-0c55b159cbfafe1f0"
        instance_type = "t2.micro"

        tags = {
          <caret>Name = "example-instance"
        }
      }
    """.trimIndent())

    myFixture.performEditorAction(IdeActions.ACTION_COMMENT_LINE)

    myFixture.checkResult("""
      resource "aws_instance" "example" {
        ami           = "ami-0c55b159cbfafe1f0"
        instance_type = "t2.micro"

        tags = {
          # Name = "example-instance"
        }
      }
    """.trimIndent())
  }
}