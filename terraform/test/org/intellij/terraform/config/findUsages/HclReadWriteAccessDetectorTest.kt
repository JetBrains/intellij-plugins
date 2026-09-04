// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.findUsages

import com.intellij.codeInsight.highlighting.ReadWriteUtil
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.descendantsOfType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hcl.psi.getNameElementUnquoted

internal class HclReadWriteAccessDetectorTest : BasePlatformTestCase() {

  fun testVariableAccessUsages() {
    val variables = myFixture.addFileToProject("modules/app/variables.tf", """
      variable "bucket_name" {
        type = string
      }
    """.trimIndent())
    myFixture.addFileToProject("modules/app/usage.tf", """
      resource "aws_s3_bucket" "this" {
        bucket = var.bucket_name
      }
      output "name" {
        value = var.bucket_name
      }
    """.trimIndent())

    myFixture.addFileToProject("main.tf", """
      module "app" {
        source      = "./modules/app"
        bucket_name = "example"
      }
    """.trimIndent())

    val variableBlock = findHclBlockInFile(variables, "variable", "bucket_name")
    assertAccess("""
      main.tf: bucket_name = "example" -> Write
      usage.tf: bucket = var.bucket_name -> Read
      usage.tf: value = var.bucket_name -> Read
    """.trimIndent(), variableBlock)
  }

  fun testLocalAccessUsages() {
    myFixture.configureByText("simple.tf", """
      locals {
        pre<caret>fix = "some_prefix"
      }
      output "echo" {
        value = local.prefix
      }
    """.trimIndent())

    assertAccess("simple.tf: value = local.prefix -> Read", myFixture.elementAtCaret)
  }

  fun testResourceAccessUsages() {
    val config = myFixture.configureByText("simple.tf", """
      resource "aws_s3_bucket" "this" {
        bucket = "some_bucket"
      }
      output "arn" {
        value = aws_s3_bucket.this.arn
      }
    """.trimIndent())

    val resourceBlock = findHclBlockInFile(config, "resource", "this")
    assertAccess("simple.tf: value = aws_s3_bucket.this.arn -> Read", resourceBlock)
  }

  fun testDataSourceAccessUsages() {
    myFixture.configureByText("simple.tf", """
      data "aws_ami" "ubu<caret>ntu" {
        most_recent = true
      }
      output "id" {
        value = data.aws_ami.ubuntu.id
      }
    """.trimIndent())

    assertAccess("simple.tf: value = data.aws_ami.ubuntu.id -> Read", myFixture.elementAtCaret)
  }

  fun testUnknownTargetStaysUnclassified() {
    val config = myFixture.configureByText("simple.tf", """
      resource "aws_s3_bucket" "this" {
        bucket = "b"
      }
    """.trimIndent())

    val property = config.descendantsOfType<HCLProperty>().first { it.name == "bucket" }
    assertNull(ReadWriteUtil.getReadWriteAccess(arrayOf(property), property))
  }

  private fun assertAccess(expected: String, target: PsiElement) {
    val accessReport = myFixture.findUsages(target)
      .mapNotNull { it.element }
      .map { "${describe(it)} -> ${ReadWriteUtil.getReadWriteAccess(arrayOf(target), it) ?: "unclassified"}" }
      .sorted()
      .joinToString("\n")

    assertEquals(expected, accessReport)
  }

  fun describe(element: PsiElement): String {
    val file = element.containingFile
    val document = PsiDocumentManager.getInstance(project).getDocument(file)
                   ?: throw AssertionError("Document not found for file: ${file.name}")

    val line = document.getLineNumber(element.textRange.startOffset)
    val text = document.getText(TextRange(document.getLineStartOffset(line), document.getLineEndOffset(line)))
    return "${file.name}: ${text.trim()}"
  }

  private fun findHclBlockInFile(file: PsiFile, type: String, name: String): HCLBlock =
    file.childrenOfType<HCLBlock>().first { it.getNameElementUnquoted(0) == type && it.name == name }
}
