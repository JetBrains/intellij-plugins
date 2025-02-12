package org.intellij.terraform.config.documentation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationAction
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.startOffset
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.terraform.config.psi.TfDocumentPsi
import org.intellij.terraform.hcl.psi.HCLBlock

internal class DocReferenceTestCase : BasePlatformTestCase() {

  fun testCorrectDocReferencesPresent() {
    val terraformConfigFile = myFixture.configureByText("main.tf", """
        terraform {
          required_providers {
            docker = {
              source  = "kreuzwerker/docker"
              version = "~> 3.0.2"
            }
          }
          required_version = "~> 1.2"
        }
        provider "docker" {
          host = "npipe:////./pipe/docker_engine"
        }
        data "docker_image" "foo" {
          name = "image1"
        }
        variable "foo" {
          type = string
        }
        resource "docker_container" "foo" {
          triggers = {
            foo = var.foo
          }
          image = "busybox"
          name  = "mycontainer"
        }
        output "foo" {
          value = var.foo
        }
      """.trimIndent())
    val namedConfigBlocks = terraformConfigFile.childrenOfType<HCLBlock>().filter { it.nameElements.size > 1 }
    assertSize(5, namedConfigBlocks)
    val (documented, unDocumented) = namedConfigBlocks.map { it.nameElements[1] }.partition {
      val offset = it.startOffset + 1
      myFixture.editor.caretModel.moveToOffset(offset)
      myFixture.elementAtCaret is TfDocumentPsi
    }
    assertSize(3, documented)
    assertSize(2, unDocumented)
    assertSameElements(documented.map { StringUtil.unquoteString(it.text) }, listOf("docker", "docker_image", "docker_container"))
    assertSameElements(unDocumented.map { StringUtil.unquoteString(it.text) }, listOf("foo", "foo"))
  }

  fun testDocReferenceDoesntBreakVarDeclaration() {
    myFixture.configureByText("main.tf", """
        variable "fo<caret>o" {
          type = string
        }        
        resource "docker_container" "foo" {
          image = "nginx:latest"
          name  = var.foo
        }
        import {
          id = "875c4869a54412110fe2d5daa0673b423e5cbeb58482869c90689333c5be6578"
          to = docker_container.foo
        }
        output "foo" {
          value       = docker_container.foo.container_logs
          description = var.foo
        }
      """.trimIndent())
    val elementAtCaret = myFixture.elementAtCaret
    val findUsages = myFixture.findUsages(elementAtCaret)
    UsefulTestCase.assertSize(2, findUsages)
    findUsages.forEach { usage ->
      assertEquals(usage.element?.text, "foo")
    }
    val targetElements = GotoDeclarationAction.findAllTargetElements(project, myFixture.editor, myFixture.caretOffset)
    UsefulTestCase.assertSize(0, targetElements)
  }

  fun testDocReferenceDoesntBreakVarReference() {
    myFixture.configureByText("main.tf", """
        variable "foo" {
          type = string
        }        
        resource "docker_container" "foo" {
          image = "nginx:latest"
          name  = var.foo
        }
        import {
          id = "875c4869a54412110fe2d5daa0673b423e5cbeb58482869c90689333c5be6578"
          to = docker_container.foo
        }
        output "foo" {
          value       = docker_container.foo.container_logs
          description = var.fo<caret>o
        }
      """.trimIndent())
    val elementAtCaret = myFixture.elementAtCaret
    val findUsages = myFixture.findUsages(elementAtCaret)
    assertSize(2, findUsages)
    findUsages.forEach { usage ->
      assertEquals(usage.element?.text, "foo")
    }
    val targetElements = GotoDeclarationAction.findAllTargetElements(project, myFixture.editor, myFixture.caretOffset)
    assertSize(1, targetElements)
    assertEquals(targetElements[0].text, """
        variable "foo" {
          type = string
        }
    """.trimIndent())
  }

  fun testDocReferenceDoesntBreakBlockDeclaration() {
    myFixture.configureByText("main.tf", """
        variable "foo" {
          type = string
        }        
        resource "docker_container" "f<caret>oo" {
          image = "nginx:latest"
          name  = var.foo
        }
        import {
          id = "875c4869a54412110fe2d5daa0673b423e5cbeb58482869c90689333c5be6578"
          to = docker_container.foo
        }
        output "foo" {
          value       = docker_container.foo.container_logs
          description = var.foo
        }
      """.trimIndent())
    val elementAtCaret = myFixture.elementAtCaret
    val findUsages = myFixture.findUsages(elementAtCaret)
    UsefulTestCase.assertSize(2, findUsages)
    findUsages.forEach { usage ->
      assertEquals(usage.element?.text, "foo")
    }
    val targetElements = GotoDeclarationAction.findAllTargetElements(project, myFixture.editor, myFixture.caretOffset)
    UsefulTestCase.assertSize(0, targetElements)
  }

  fun testDocReferenceDoesntBreakBlockReference() {
    myFixture.configureByText("main.tf", """
        variable "foo" {
          type = string
        }        
        resource "docker_container" "foo" {
          image = "nginx:latest"
          name  = var.foo
        }
        import {
          id = "875c4869a54412110fe2d5daa0673b423e5cbeb58482869c90689333c5be6578"
          to = docker_container.fo<caret>o
        }
        output "foo" {
          value       = docker_container.foo.container_logs
          description = var.foo
        }
      """.trimIndent())
    val elementAtCaret = myFixture.elementAtCaret
    val findUsages = myFixture.findUsages(elementAtCaret)
    UsefulTestCase.assertSize(2, findUsages)
    findUsages.forEach { usage ->
      assertEquals(usage.element?.text, "foo")
    }
    val targetElements = GotoDeclarationAction.findAllTargetElements(project, myFixture.editor, myFixture.caretOffset)
    UsefulTestCase.assertSize(1, targetElements)
    assertEquals(targetElements[0].text, """
        resource "docker_container" "foo" {
          image = "nginx:latest"
          name  = var.foo
        }
    """.trimIndent())
  }


}