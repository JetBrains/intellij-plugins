// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model

import com.intellij.psi.PsiManager
import com.intellij.testFramework.LightVirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.FileContentUtilCore

class GetContainingFileTest : BasePlatformTestCase() {


  fun testDetectPhysicalFile() {
    myFixture.configureByText("main.tf", """
      resource "exa<caret>mple" "example" {
        for_each = {
        for index, vm in local.virtual_machines :
        vm.name => vm
        }
        name       = each.value.name
        ip_address = each.value.ip_address
      }
    """.trimIndent())
    val message = "We should be able to detect containing file for an ordinary PSI File"
    val containingFile = getContainingFile(myFixture.elementAtCaret) ?: throw AssertionError(message)
    assertEquals(message, "main.tf", containingFile.name)
  }

  fun testDetectLightVirtualFile() {
    val virtualFile = LightVirtualFile("main.tf", """
      resource "example" "example" {
        for_each = {
        for index, vm in local.virtual_machines :
        vm.name => vm
        }
        name       = each.value.name
        ip_address = each.value.ip_address
      }
    """.trimIndent())
    val message = "We should be able to detect containing file for an Light Virtual File"
    val psiFile = PsiManager.getInstance(project).findFile(virtualFile) ?: throw AssertionError(message)
    val containingFile = getContainingFile(psiFile.firstChild.firstChild) ?: throw AssertionError(message)
    assertEquals(message, "main.tf", containingFile.name)
  }

  fun testDetectWithInvalidFile() {
    val psiFile = myFixture.configureByText("main.tf", """
      resource "example" "example" {
        for_each = {
        for index, vm in local.virtual_machines :
        vm.name => vm
        }
        name       = each.value.name
        ip_<caret>address = each.value.ip_address
      }
    """.trimIndent())
    val psiElement = myFixture.elementAtCaret
    //Let's invalidate the PSI file
    FileContentUtilCore.reparseFiles(psiFile.virtualFile)
    val containingFile = getContainingFile(psiElement)
    assertNull("Should not detect containing file for an invalid PSI element", containingFile)
  }


}