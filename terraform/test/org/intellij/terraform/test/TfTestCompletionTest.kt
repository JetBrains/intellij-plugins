// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.test

import com.intellij.lang.Language
import org.intellij.terraform.config.CompletionTestCase
import org.intellij.terraform.config.codeinsight.TfConfigCompletionTest.Companion.assertNoTfRootBlocks
import org.intellij.terraform.hcl.HCLLanguage
import org.intellij.terraform.stack.TfComponentCompletionTest.Companion.assertNoTfComponentBlocks
import org.intellij.terraform.stack.TfDeployCompletionTest.Companion.assertNoTfDeployBlocks
import org.intellij.terraform.terragrunt.TerragruntCompletionTest.Companion.assertNoStackBlocks
import org.intellij.terraform.terragrunt.TerragruntCompletionTest.Companion.assertNoTerragruntBlocks
import org.intellij.terraform.terragrunt.doAutoInsertCompletionTest

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

  companion object {
    private val TfTestRootBlockKeywords: List<String> = TfTestRootBlocks.map { it.name }
  }
}
