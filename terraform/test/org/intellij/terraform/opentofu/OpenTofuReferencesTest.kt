// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.opentofu

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.getNameElementUnquoted

internal class OpenTofuReferencesTest: BasePlatformTestCase()  {

  fun testEncryptionMethodProviderReference() {
     myFixture.configureByText("main.tofu", """
       terraform {
         encryption {
           key_provider "some_key_provider" "some_name" {
           }

           method "some_method" "some_method_name" {
             keys = key_provider.some_key_provider.s<caret>ome_name
           }

           state {
             method = method.some_method.some_method_name
           }

           plan {
             method = method.some_method.some_method_name
           }
         }
       }
     """.trimIndent())
    val psiElement = myFixture.elementAtCaret
    assertTrue(psiElement is HCLBlock)
    assertNotNull( (psiElement as HCLBlock).`object`)
    assertEquals(psiElement.getNameElementUnquoted(1), "some_key_provider")
    assertEquals(psiElement.getNameElementUnquoted(2), "some_name")
  }

  fun testEncryptionStateMethodReference() {
    myFixture.configureByText("main.tofu", """
       terraform {
         encryption {
           key_provider "some_key_provider" "some_name" {
           }

           method "some_method" "some_method_name" {
             keys = key_provider.some_key_provider.some_name
           }

           state {
             method = method.some_method.some_me<caret>thod_name
           }

           plan {
             method = method.some_method.some_method_name
           }
         }
       }
     """.trimIndent())
    val psiElement = myFixture.elementAtCaret
    assertTrue(psiElement is HCLBlock)
    assertNotNull( (psiElement as HCLBlock).`object`)
    assertEquals(psiElement.getNameElementUnquoted(1), "some_method")
    assertEquals(psiElement.getNameElementUnquoted(2), "some_method_name")
  }

  fun testEncryptionStateFallbackMethodReference() {
    myFixture.configureByText("main.tofu", """
       terraform {
         encryption {
           key_provider "some_key_provider" "some_name" {
           }

           method "unencrypted" "migrate" {}

           method "some_method" "some_method_name" {
             keys = key_provider.some_key_provider.some_name
           }

           state {
             method = method.some_method.some_method_name
             fallback {
               method = method.unencrypted.mig<caret>rate
             }
           }

           plan {
             method = method.some_method.some_method_name
           }
         }
       }
     """.trimIndent())
    val psiElement = myFixture.elementAtCaret
    assertTrue(psiElement is HCLBlock)
    assertNotNull( (psiElement as HCLBlock).`object`)
    assertEquals(psiElement.getNameElementUnquoted(1), "unencrypted")
    assertEquals(psiElement.getNameElementUnquoted(2), "migrate")
  }


  fun testEncryptionPlanMethodReference() {
    myFixture.configureByText("main.tofu", """
       terraform {
         encryption {
           key_provider "some_key_provider" "some_name" {
           }

           method "some_method" "some_method_name" {
             keys = key_provider.some_key_provider.some_name
           }

           state {
             method = method.some_method.some_method_name
           }

           plan {
             method = method.some_method.some_me<caret>thod_name
           }
         }
       }
     """.trimIndent())
    val psiElement = myFixture.elementAtCaret
    assertTrue(psiElement is HCLBlock)
    assertNotNull( (psiElement as HCLBlock).`object`)
    assertEquals(psiElement.getNameElementUnquoted(1), "some_method")
    assertEquals(psiElement.getNameElementUnquoted(2), "some_method_name")
  }

  fun testEncryptionPlanFallbackMethodReference() {
    myFixture.configureByText("main.tofu", """
       terraform {
         encryption {
           key_provider "some_key_provider" "some_name" {
           }

           method "unencrypted2" "migrate2" {}

           method "some_method" "some_method_name" {
             keys = key_provider.some_key_provider.some_name
           }

           state {
             method = method.some_method.some_method_name
           }

           plan {
             method = method.some_method.some_method_name
             fallback {
               method = method.unencrypted2.mig<caret>rate2
             }
           }
         }
       }
     """.trimIndent())
    val psiElement = myFixture.elementAtCaret
    assertTrue(psiElement is HCLBlock)
    assertNotNull( (psiElement as HCLBlock).`object`)
    assertEquals(psiElement.getNameElementUnquoted(1), "unencrypted2")
    assertEquals(psiElement.getNameElementUnquoted(2), "migrate2")
  }


}