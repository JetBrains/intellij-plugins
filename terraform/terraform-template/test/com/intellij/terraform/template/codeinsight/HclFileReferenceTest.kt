package com.intellij.terraform.template.codeinsight

import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.openapi.util.TextRange
import com.intellij.platform.testFramework.assertion.collectionAssertion.CollectionAssertions.assertContainsUnordered
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightProjectFixture
import com.intellij.terraform.template.HclFileReference
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private const val dollar = "$"

@TestApplication
class HclFileReferenceTest {
  private val projectFixture = codeInsightProjectFixture()
  private val myFixture by codeInsightFixture(projectFixture)

  @Test
  fun `test file reference present in templatefile function`() {
    myFixture.configureByText("main.tf", """
      resource "aws_instance" "demo_vm" {
        user_data = templatefile("abc/<caret>def", { request_id = "REQ000129834", name = "John" })
      }
    """.trimIndent())
    val fileReference = myFixture.getReferenceAtCaretPosition("main.tf")
    assertTrue(fileReference is HclFileReference)
    assertEquals(TextRange.create(1, 8), fileReference!!.rangeInElement)
  }

  @Test
  fun `test file reference resolve in plain string`() {
    val expectedReferenceTarget = myFixture.addFileToProject("/foo/bar/buzz.tftpl", "")
    myFixture.configureByText("main.tf", """
        resource "aws_instance" "demo_vm" {
          user_data = templatefile("foo/bar/buzz<caret>.tftpl")
        }
      """.trimIndent())
    val fileReference = myFixture.getReferenceAtCaretPosition("main.tf")
    assertTrue(fileReference is HclFileReference)
    assertEquals(expectedReferenceTarget, runReadActionBlocking { fileReference!!.resolve() })
  }

  @Test
  fun `test file reference resolve works in string template`() {
    // resolve uses a pretty simple heuristic that could be improved by implementing string evaluator
    val expectedReferenceTarget = myFixture.addFileToProject("/foo/bar/buzz.tftpl", "")
    myFixture.configureByText("main.tf", """
        resource "aws_instance" "demo_vm" {
          user_data = templatefile("$dollar{var.example}/buzz<caret>.tftpl")
        }
      """.trimIndent())
    val fileReference = myFixture.getReferenceAtCaretPosition("main.tf")
    assertTrue(fileReference is HclFileReference)
    assertEquals(expectedReferenceTarget, runReadActionBlocking { fileReference!!.resolve() })
  }

  @Test
  fun `test template reference completion`() {
    myFixture.addFileToProject("root/template1.tftpl", "")
    myFixture.addFileToProject("root/template2.tftpl", "")
    myFixture.addFileToProject("root/main.tf", """
      resource "aws_instance" "demo_vm" {
        user_data = templatefile("<caret>")
      }
    """.trimIndent())
    val completionVariants = myFixture.getCompletionVariants("root/main.tf")
    assertNotNull(completionVariants)
    assertContainsUnordered(listOf("template1.tftpl", "template2.tftpl"), completionVariants!!)
  }

  @Test
  fun `test nested folder template reference completion`() {
    myFixture.addFileToProject("root/subdir/template1.tftpl", "")
    myFixture.addFileToProject("root/subdir/template2.tftpl", "")
    myFixture.addFileToProject("root/main.tf", """
      resource "aws_instance" "demo_vm" {
        user_data = templatefile("<caret>")
      }
    """.trimIndent())
    val completionVariants = myFixture.getCompletionVariants("root/main.tf")
    assertNotNull(completionVariants)
    assertContainsUnordered(listOf("template1.tftpl", "template2.tftpl"), completionVariants!!)
  }

  @Test
  fun `test nested folder template relative path extracted correctly`(): Unit = timeoutRunBlocking {
    myFixture.addFileToProject("root/subdir/template1.tftpl", "")
    myFixture.addFileToProject("root/main.tf", """
      resource "aws_instance" "demo_vm" {
        user_data = templatefile("<caret>")
      }
    """.trimIndent())
    myFixture.configureByFile("root/main.tf")
    withContext(Dispatchers.EDT) {
      myFixture.completeBasicAllCarets('\n')
    }
    myFixture.checkResult("""
      resource "aws_instance" "demo_vm" {
        user_data = templatefile("$dollar{path.module}/subdir/template1.tftpl")
      }
    """.trimIndent())
  }
}