// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.stack

import com.intellij.lang.Language
import com.intellij.openapi.util.registry.Registry
import org.intellij.terraform.config.CompletionTestCase
import org.intellij.terraform.config.codeinsight.COMPLETION_VARIANTS_LIMIT
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.RootBlockKeywords
import org.intellij.terraform.config.codeinsight.TfConfigCompletionTest
import org.intellij.terraform.hcl.HCLLanguage
import org.intellij.terraform.stack.component.TF_COMPONENT_EXTENSION
import org.intellij.terraform.stack.component.TfComponentRootBlocks
import org.intellij.terraform.terragrunt.TerragruntCompletionTest.Companion.TerragruntBlockKeywords
import org.intellij.terraform.terragrunt.doAutoInsertCompletionTest

internal class TfComponentCompletionTest : CompletionTestCase() {
  override fun setUp() {
    super.setUp()
    Registry.get("ide.completion.variant.limit").setValue(COMPLETION_VARIANTS_LIMIT, testRootDisposable)
  }

  override fun getFileName(): String = "test$TF_COMPONENT_EXTENSION"
  override fun getExpectedLanguage(): Language = HCLLanguage
  override fun runInDispatchThread(): Boolean = false

  fun testKeywordsCompletionInArray() {
    doBasicCompletionTest("""
      component "test_component" {
        some_array_property = [<caret>]
      }
    """.trimIndent(), "null", "true", "false")

    doBasicCompletionTest("""
      component "test_component" {
        some_array_property = [component.some_component, <caret>]
      }
    """.trimIndent(), "null", "true", "false")
  }

  fun testTfComponentRootBlocksCompletion() {
    doBasicCompletionTest("<caret> ", TfComponentBlockKeywords)
    doBasicCompletionTest("<caret> {}", TfComponentBlockKeywords)
    doBasicCompletionTest("locals {}\n<caret> ", TfComponentBlockKeywords)
    doBasicCompletionTest("locals {}\n<caret> {}", TfComponentBlockKeywords)
  }

  fun testNotAllowedRootBlocksInTfComponent() {
    val file = myFixture.configureByText(fileName, "<caret>")
    val completionVariants = myFixture.getCompletionVariants(file.virtualFile.name).orEmpty()
    assertNotEmpty(completionVariants)

    val commonRootBlocks = listOf("provider", "removed", "output", "variable", "locals")
    val unexpectedTerraformBlocks = RootBlockKeywords.filter { it in completionVariants && it !in commonRootBlocks }
    assertTrue(
      "These Terraform-only root blocks should not appear in a Tf Component file: $unexpectedTerraformBlocks",
      unexpectedTerraformBlocks.isEmpty()
    )

    val unexpectedTerragruntBlocks = TerragruntBlockKeywords.filter { it in completionVariants && it !in commonRootBlocks }
    assertTrue(
      "These Terragrunt-only root blocks should not appear in a Tf Component file: $unexpectedTerragruntBlocks",
      unexpectedTerragruntBlocks.isEmpty()
    )
  }

  fun testAutoInsertCompletion() {
    doAutoInsertCompletionTest(myFixture, fileName, "remo<caret>", """
      removed {
        from   = ""
        providers = {}
        source = ""
      }
    """.trimIndent())

    doAutoInsertCompletionTest(myFixture, fileName, "var", """
      variable "" {
        type = ""
      }
    """.trimIndent())
  }

  fun testPropertiesCompletion() {
    doBasicCompletionTest("component \"component1\" { <caret> }", 6,
                          "source", "version", "inputs", "providers", "depends_on", "for_each")

    doBasicCompletionTest("output \"test1\" { <caret> }", Matcher.and(
      Matcher.all("type", "value", "description", "sensitive", "ephemeral"),
      Matcher.not("precondition"))
    )

    doBasicCompletionTest("""
      provider "aws" "main" {
        for_each = var.regions
        <caret>
      }
    """.trimIndent(), "config")
  }

  fun testRequiredProvidersCompletion() {
    val matcher = getPartialMatcher(TfConfigCompletionTest.collectBundledProviders())
    doBasicCompletionTest("required_providers { <caret> }", matcher)
    doBasicCompletionTest("""
      required_providers {
        <caret>
      }
    """.trimIndent(), "aws", "azurerm", "google", "kubernetes", "alicloud", "oci")

    // Properties of required_provider completion
    doBasicCompletionTest("""
      required_providers {
        kubernetes = {
          <caret>
        }
      }
    """.trimIndent(), 2, "source", "version")
    doBasicCompletionTest("""
      required_providers {
        kubernetes = {
          source = "hashicorp/kubernetes"
          <caret>
        }
      }
    """.trimIndent(), 1, "version")
  }

  companion object {
    val TfComponentBlockKeywords: List<String> = TfComponentRootBlocks.map { it.name }
  }
}