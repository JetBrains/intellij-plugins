// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.stack

import com.intellij.lang.Language
import com.intellij.openapi.util.registry.Registry
import org.intellij.terraform.config.CompletionTestCase
import org.intellij.terraform.config.codeinsight.COMPLETION_VARIANTS_LIMIT
import org.intellij.terraform.config.codeinsight.TfConfigCompletionTest
import org.intellij.terraform.config.codeinsight.TfConfigCompletionTest.Companion.assertNoTfRootBlocks
import org.intellij.terraform.hcl.HCLLanguage
import org.intellij.terraform.stack.TfDeployCompletionTest.Companion.assertNoTfDeployBlocks
import org.intellij.terraform.stack.component.TF_COMPONENT_EXTENSION
import org.intellij.terraform.stack.component.TfComponentRootBlocks
import org.intellij.terraform.terragrunt.TerragruntCompletionTest.Companion.assertNoStackBlocks
import org.intellij.terraform.terragrunt.TerragruntCompletionTest.Companion.assertNoTerragruntBlocks
import org.intellij.terraform.terragrunt.doAutoInsertCompletionTest

internal class TfComponentCompletionTest : CompletionTestCase() {
  override fun setUp() {
    super.setUp()
    Registry.get("ide.completion.variant.limit").setValue(COMPLETION_VARIANTS_LIMIT, testRootDisposable)
  }

  override fun getFileName(): String = "test.$TF_COMPONENT_EXTENSION"
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

    val fileExtension = TF_COMPONENT_EXTENSION
    assertNoTfRootBlocks(fileExtension, completionVariants, listOf("provider", "locals", "output", "removed", "variable"))
    assertNoTfDeployBlocks(fileExtension, completionVariants)

    assertNoTerragruntBlocks(fileExtension, completionVariants, listOf("provider", "locals"))
    assertNoStackBlocks(fileExtension, completionVariants, listOf("locals", "stack"))
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

  fun testComponentInputsCompletion() {
    myFixture.addFileToProject("aws-eks-fargate/variables.tf", """
      variable "vpc_id" {
        type = string
      }
      variable "kubernetes_version" {
        type = string
      }
      variable "tfc_hostname" {
        type    = string
        default = "https://app.terraform.io"
      }
      variable "tfc_kubernetes_audience" {
        type = string
      }
    """.trimIndent())

    doBasicCompletionTest("""
      component "eks" {
        source = "./aws-eks-fargate"
        inputs = {
          <caret>
        }
      }
    """.trimIndent(), "vpc_id", "kubernetes_version", "tfc_hostname", "tfc_kubernetes_audience")
    doBasicCompletionTest("""
      component "eks" {
        source = "./aws-eks-fargate"
        inputs = {
          tfc_hostname = var.tfc_hostname
          t<caret>
        }
      }
    """.trimIndent(), "tfc_kubernetes_audience", "kubernetes_version")
  }

  fun testInputsCompletionInStack() {
    myFixture.addFileToProject("aws-eks-addon/main.tf", """
      locals {
        tags = {
          Blueprint = var.cluster_name
        }
      }

      variable "vpc_id" {
        type    = string
      }
      variable "cluster_name" {
        type    = string
      }
      variable "cluster_endpoint" {
        type    = string
      }
    """.trimIndent())

    doBasicCompletionTest("""
      stack "test" {
        inputs = { <caret> }
        source  = "./aws-eks-addon"
      }
    """.trimIndent(), "vpc_id", "cluster_name", "cluster_endpoint")
  }

  fun testComponentProvidersCompletion() {
    myFixture.addFileToProject("aws-vpc/providers.tf", """
      terraform {
        required_providers {
          aws = {
            source  = "hashicorp/aws"
            version = "~> 5.0"
          }
          kubernetes = {
            source  = "hashicorp/kubernetes"
            version = "2.38.0"
          }
        }
      }
    """.trimIndent())

    doBasicCompletionTest("""
      component "vpc" {
        source = "./aws-vpc"
        providers = { <caret> }
      }
    """.trimIndent(), "aws", "kubernetes")
    doBasicCompletionTest("""
      component "vpc" {
        source = "./aws-vpc"
        providers = {
          aws = provider.aws.configurations[each.value]
          <caret>
        }
      }
    """.trimIndent(), "kubernetes")
  }

  companion object {
    val TfComponentBlockKeywords: List<String> = TfComponentRootBlocks.map { it.name }

    fun assertNoTfComponentBlocks(
      fileExtension: String,
      completionVariants: List<String>,
      commonRootBlocks: List<String> = emptyList(),
    ) {
      val unexpectedBlocks = TfComponentBlockKeywords.filter { it in completionVariants && it !in commonRootBlocks }
      assertTrue(
        "These Tf Component-only root blocks should not appear in $fileExtension file: $unexpectedBlocks",
        unexpectedBlocks.isEmpty()
      )
    }
  }
}