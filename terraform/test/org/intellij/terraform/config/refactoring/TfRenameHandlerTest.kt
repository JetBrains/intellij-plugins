// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.refactoring

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class TfRenameHandlerTest : BasePlatformTestCase() {
  fun testRenameResource() {
    doTest(
      """
      resource "aws_instance" "<caret>old_name" {
        ami = "ami-123456"
      }
      """.trimIndent(),
      "new_name",
      """
      resource "aws_instance" "new_name" {
        ami = "ami-123456"
      }
      """.trimIndent()
    )
  }

  fun testRenameDataSource() {
    doTest(
      """
      data "aws_iam_role" "<caret>old_role" {
        name = "my-role"
      }

      resource "aws_lambda_function" "lambda" {
        role = data.aws_iam_role.old_role.arn
      }
      """.trimIndent(),
      "new_role",
      """
      data "aws_iam_role" "new_role" {
        name = "my-role"
      }

      resource "aws_lambda_function" "lambda" {
        role = data.aws_iam_role.new_role.arn
      }
      """.trimIndent()
    )
  }

  fun testRenameLocalVariable() {
    doTest(
      """
      locals {
        old_local<caret> = "some_value"
      }

      resource "aws_instance" "example" {
        tags = {
          Name = local.old_local
        }
      }
      """.trimIndent(),
      "new_local",
      """
      locals {
        new_local = "some_value"
      }

      resource "aws_instance" "example" {
        tags = {
          Name = local.new_local
        }
      }
      """.trimIndent()
    )
  }

  private fun doTest(before: String, newName: String, after: String) {
    myFixture.configureByText("main.tf", before)
    myFixture.renameElementAtCaret(newName)
    myFixture.checkResult(after)
  }
}
