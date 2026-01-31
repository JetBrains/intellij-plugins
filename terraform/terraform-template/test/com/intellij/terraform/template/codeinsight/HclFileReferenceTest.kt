package com.intellij.terraform.template.codeinsight

import com.intellij.openapi.util.TextRange
import com.intellij.terraform.template.HclFileReference
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Assert

private const val dollar = "$"

class HclFileReferenceTest : BasePlatformTestCase() {
  fun `test file reference present in templatefile function`() {
    myFixture.configureByText("main.tf", """
      resource "aws_instance" "demo_vm" {
        user_data = templatefile("abc/<caret>def", { request_id = "REQ000129834", name = "John" })
      }
    """.trimIndent())
    val fileReference = myFixture.getReferenceAtCaretPosition("main.tf")
    Assert.assertTrue(fileReference is HclFileReference)
    Assert.assertEquals(TextRange.create(1, 8), fileReference!!.rangeInElement)
  }

  fun `test file reference resolve in plain string`() {
    val expectedReferenceTarget = myFixture.addFileToProject("/foo/bar/buzz.tftpl", "")
    myFixture.configureByText("main.tf", """
        resource "aws_instance" "demo_vm" {
          user_data = templatefile("foo/bar/buzz<caret>.tftpl")
        }
      """.trimIndent())
    val fileReference = myFixture.getReferenceAtCaretPosition("main.tf")
    Assert.assertTrue(fileReference is HclFileReference)
    Assert.assertEquals(expectedReferenceTarget, fileReference!!.resolve())
  }

  fun `test file reference resolve works in string template`() {
    // resolve uses a pretty simple heuristic that could be improved by implementing string evaluator
    val expectedReferenceTarget = myFixture.addFileToProject("/foo/bar/buzz.tftpl", "")
    myFixture.configureByText("main.tf", """
        resource "aws_instance" "demo_vm" {
          user_data = templatefile("$dollar{var.example}/buzz<caret>.tftpl")
        }
      """.trimIndent())
    val fileReference = myFixture.getReferenceAtCaretPosition("main.tf")
    Assert.assertTrue(fileReference is HclFileReference)
    Assert.assertEquals(expectedReferenceTarget, fileReference!!.resolve())
  }

  fun `test template reference completion`() {
    myFixture.addFileToProject("root/template1.tftpl", "")
    myFixture.addFileToProject("root/template2.tftpl", "")
    myFixture.addFileToProject("root/main.tf", """
      resource "aws_instance" "demo_vm" {
        user_data = templatefile("<caret>")
      }
    """.trimIndent())
    val completionVariants = myFixture.getCompletionVariants("root/main.tf")
    Assert.assertNotNull(completionVariants)
    assertContainsElements(completionVariants!!, "template1.tftpl", "template2.tftpl")
  }

  fun `test nested folder template reference completion`() {
    myFixture.addFileToProject("root/subdir/template1.tftpl", "")
    myFixture.addFileToProject("root/subdir/template2.tftpl", "")
    myFixture.addFileToProject("root/main.tf", """
      resource "aws_instance" "demo_vm" {
        user_data = templatefile("<caret>")
      }
    """.trimIndent())
    val completionVariants = myFixture.getCompletionVariants("root/main.tf")
    Assert.assertNotNull(completionVariants)
    assertContainsElements(completionVariants!!, "template1.tftpl", "template2.tftpl")
  }

  fun `test nested folder template relative path extracted correctly`() {
    myFixture.addFileToProject("root/subdir/template1.tftpl", "")
    myFixture.addFileToProject("root/main.tf", """
      resource "aws_instance" "demo_vm" {
        user_data = templatefile("<caret>")
      }
    """.trimIndent())
    myFixture.configureByFile("root/main.tf")
    myFixture.completeBasicAllCarets('\n')
    myFixture.checkResult("""
      resource "aws_instance" "demo_vm" {
        user_data = templatefile("$dollar{path.module}/subdir/template1.tftpl")
      }
    """.trimIndent())
  }
}