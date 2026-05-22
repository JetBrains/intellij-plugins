// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.test

import com.intellij.lang.Language
import org.intellij.terraform.config.CompletionTestCase
import org.intellij.terraform.config.codeinsight.TfConfigCompletionTest
import org.intellij.terraform.config.codeinsight.TfConfigCompletionTest.Companion.assertNoTfRootBlocks
import org.intellij.terraform.hcl.HCLLanguage
import org.intellij.terraform.stack.TfComponentCompletionTest.Companion.assertNoTfComponentBlocks
import org.intellij.terraform.stack.TfDeployCompletionTest.Companion.assertNoTfDeployBlocks
import org.intellij.terraform.terragrunt.TerragruntCompletionTest.Companion.assertNoStackBlocks
import org.intellij.terraform.terragrunt.TerragruntCompletionTest.Companion.assertNoTerragruntBlocks
import org.intellij.terraform.terragrunt.doAutoInsertCompletionTest
import org.intellij.terraform.test.model.TfTestRootBlocks

internal class TfTestCompletionTest : CompletionTestCase() {
  override fun getFileName(): String = "main.$TF_TEST_EXTENSION"
  override fun getExpectedLanguage(): Language = HCLLanguage
  override fun runInDispatchThread(): Boolean = false

  fun testRootBlocksCompletion() {
    doBasicCompletionTest("<caret> ", TfTestRootBlockKeywords)
    doBasicCompletionTest("<caret> {}", TfTestRootBlockKeywords)
    doBasicCompletionTest("variables {}\n<caret> ", TfTestRootBlockKeywords)
    doBasicCompletionTest("variables {}\n<caret> {}", TfTestRootBlockKeywords)
  }

  fun testAllowedRootBlocksInTfTest() {
    val file = myFixture.configureByText(fileName, "<caret>")
    val completionVariants = myFixture.getCompletionVariants(file.virtualFile.name).orEmpty()
    assertNotEmpty(completionVariants)

    val fileExtension = TF_TEST_EXTENSION
    assertNoTfRootBlocks(fileExtension, completionVariants, listOf("provider"))
    assertNoTfComponentBlocks(fileExtension, completionVariants, listOf("provider"))
    assertNoTfDeployBlocks(fileExtension, completionVariants, emptyList())

    assertNoTerragruntBlocks(fileExtension, completionVariants, listOf("provider"))
    assertNoStackBlocks(fileExtension, completionVariants)
  }

  fun testAllowedRootBlocksInTfMock() {
    val file = myFixture.configureByText("aws.$TF_MOCK_EXTENSION", "<caret>")
    val completionVariants = myFixture.getCompletionVariants(file.virtualFile.name).orEmpty()
    assertNotEmpty(completionVariants)

    assertContainsElements(completionVariants, "mock_resource", "mock_data")

    val fileExtension = TF_MOCK_EXTENSION
    assertNoTfRootBlocks(fileExtension, completionVariants)
    assertNoTfComponentBlocks(fileExtension, completionVariants)
    assertNoTfDeployBlocks(fileExtension, completionVariants, emptyList())
  }

  fun testAutoInsertCompletion() {
    doAutoInsertCompletionTest(myFixture, fileName, "run \"valid_string_concat\" {\n  as<caret>\n}", """
      run "valid_string_concat" {
        assert {
          condition     = false
          error_message = ""
        }
      }
    """.trimIndent())

    doAutoInsertCompletionTest(myFixture, fileName, "override_da<caret>", """
      override_data {
        target = ""
      }
    """.trimIndent())
  }

  fun testRunBlockPropertiesCompletion() {
    doBasicCompletionTest("""
      run "create_test_infrastructure" {
        <caret>
      }
    """.trimIndent(), "assert", "plan_options", "variables", "module", "providers", "command", "expect_failures", "state_key", "parallel")
    doBasicCompletionTest("""
      run "create_nothing" {
        command = <caret>
      }
    """.trimIndent(), "apply", "plan")
    doBasicCompletionTest("""
      run "create_oidc_provider_only" {
        plan_options { <caret> }
      }
    """.trimIndent(), "mode", "refresh", "replace", "target")

    doBasicCompletionTest("""
      mock_provider "tls" {
        override_during = <caret>
      }
    """.trimIndent(), "apply", "plan")
  }

  fun testBlockTypeNameCompletion() {
    val providers = getPartialMatcher(TfConfigCompletionTest.collectBundledProviders())
    doBasicCompletionTest("provider <caret>", providers)
    doBasicCompletionTest("mock_provider <caret>", providers)
    doBasicCompletionTest("provider \"<caret>\"", providers)
    doBasicCompletionTest("mock_provider \"<caret>\"", providers)

    doBasicCompletionTest("resource \"aws_s3<caret>\"", "aws_s3_bucket", "aws_s3_access_point", "aws_s3_bucket_acl")
    doBasicCompletionTest("""
      mock_provider "aws" {
        mock_resource "aws_vp<caret>" {
      }  
    """.trimIndent(), "aws_vpc", "aws_vpc_endpoint")

    doBasicCompletionTest("""
      mock_provider "google" {
        mock_data "google_compute<>" { }
      }
    """.trimIndent(), "google_compute_address", "google_compute_disk", "google_compute_image", "google_compute_instance")
  }

  fun testProviderPropertiesCompletionInTfTest() {
    doBasicCompletionTest("""
      provider "aws" {
        access_key = "dev-tfstate-backend"
        secret_key = "dev-tfstate-backend"
        <caret>
      }
    """.trimIndent(), Matcher.and(Matcher.all("region", "assume_role", "skip_credentials_validation", "skip_requesting_account_id"),
                                  Matcher.not("access_key", "secret_key"))
    )

    doBasicCompletionTest("""
      provider "aws" {
        endpoints {
          iam = "http://localhost:4566"
          sts = "http://localhost:4566"
          <caret>
        }
      }
    """.trimIndent(), "acm", "amg", "amp")
  }

  fun testDefaultsPropertiesCompletion() {
    doBasicCompletionTest("""
      mock_provider "aws" {
        mock_resource "aws_vpc" {
          defaults = {
            id = "vpc-mock00001"
            <caret>
          }
        }     
      }
    }
    """.trimIndent(), Matcher.and(Matcher.all("arn", "cidr_block", "region"),
                                  Matcher.not("id"))
    )

    doBasicCompletionTest("""
      mock_data "azurerm_storage_account" {
        defaults = { primary_web_<caret> }
      }
    """.trimIndent(), "primary_web_host", "primary_web_endpoint", "primary_web_internet_host")
  }

  companion object {
    private val TfTestRootBlockKeywords: List<String> = TfTestRootBlocks.map { it.name }
  }
}
