// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.terragrunt

import com.intellij.lang.Language
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.common.waitUntilAssertSucceeds
import org.intellij.terraform.config.CompletionTestCase
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.RootBlockKeywords
import org.intellij.terraform.hcl.HCLLanguage
import org.intellij.terraform.terragrunt.model.StackRootBlocks
import org.intellij.terraform.terragrunt.model.TerragruntBlocksAndAttributes
import org.intellij.terraform.terragrunt.model.TerragruntFunctions

internal class TerragruntCompletionTest : CompletionTestCase() {
  override fun getFileName(): String = TERRAGRUNT_MAIN_FILE
  override fun getExpectedLanguage(): Language = HCLLanguage
  override fun runInDispatchThread(): Boolean = false

  fun testTerragruntRootBlocksCompletion() {
    doBasicCompletionTest("<caret> ", TerragruntBlockKeywords)
    doBasicCompletionTest("<caret> {}", TerragruntBlockKeywords)
    doBasicCompletionTest("generate = local.common.generate\n<caret> ", TerragruntBlockKeywords)
    doBasicCompletionTest("generate = local.common.generate\n<caret> {}", TerragruntBlockKeywords)

    doBasicCompletionTest("\"<caret> ", TerragruntBlockKeywords)
    doBasicCompletionTest("\"<caret>\" ", TerragruntBlockKeywords)
    doBasicCompletionTest("generate = local.common.generate\n\"<caret> ", TerragruntBlockKeywords)
    doBasicCompletionTest("generate = local.common.generate\n\"<caret>\" ", TerragruntBlockKeywords)

    doBasicCompletionTest("\"<caret> {}", TerragruntBlockKeywords)
    doBasicCompletionTest("\"<caret>\" {}", TerragruntBlockKeywords)
    doBasicCompletionTest("generate = local.common.generate\n\"<caret> {}", TerragruntBlockKeywords)
    doBasicCompletionTest("generate = local.common.generate\n\"<caret>\" {}", TerragruntBlockKeywords)
  }

  fun testNotAllowedRootBlockInTerragrunt() {
    val file = myFixture.configureByText("test.terragrunt.hcl", "<caret>")
    val completionVariants = myFixture.getCompletionVariants(file.virtualFile.name)
      ?.filterNot { it == "terraform" || it == "locals" }
      .orEmpty()
    assertNotEmpty(completionVariants)

    val unexpectedTerraformBlocks = RootBlockKeywords.filter { it in completionVariants }
    assertTrue(
      "These Terraform-only root blocks should not appear in a Terragrunt file: $unexpectedTerraformBlocks",
      unexpectedTerraformBlocks.isEmpty()
    )

    val unexpectedStackBlocks = StackBlockKeywords.filter { it in completionVariants }
    assertTrue(
      "These Terragrunt Stack-only root blocks should not appear in a Terragrunt file: $unexpectedStackBlocks",
      unexpectedStackBlocks.isEmpty()
    )
  }

  fun testStackRootBlocksCompletion() {
    val file = myFixture.configureByText(TERRAGRUNT_STACK_FILE, "<caret>")
    val completionVariants = myFixture.getCompletionVariants(file.virtualFile.name).orEmpty()

    assertNotEmpty(completionVariants)
    assertEquals(3, completionVariants.size)
    assertContainsElements(completionVariants, "unit", "stack", "locals")
  }

  fun testAutoInsertCompletionTest() {
    doAutoInsertCompletionTest("incl<caret>", """
      include "" {
        path = ""
      }
    """.trimIndent())
    doAutoInsertCompletionTest("gene<caret>", """
      generate "" {
        contents = ""
        path     = ""
      }
    """.trimIndent()
    )

    // Terragrunt Stack files
    doAutoInsertCompletionTest("sta<caret>", """
      stack "" {
        path   = ""
        source = ""
      }
    """.trimIndent(), TERRAGRUNT_STACK_FILE)
    doAutoInsertCompletionTest("un<caret>", """
      unit "" {
        path   = ""
        source = ""
      }
    """.trimIndent(), TERRAGRUNT_STACK_FILE)
  }

  fun testPropertiesCompletion() {
    doBasicCompletionTest("terraform { <caret> }", Matcher.and(
      Matcher.all("after_hook", "before_hook", "copy_terraform_lock_file", "error_hook", "exclude_from_copy", "extra_arguments", "include_in_copy", "source"),
      Matcher.not("required_providers"))
    )
    doBasicCompletionTest("""
      terraform {
        extra_arguments {
          arguments = []
          commands = []
          <caret>
        }
      }
    """.trimIndent(), Matcher.and(
      Matcher.all("env_vars", "required_var_files", "optional_var_files"),
      Matcher.not("arguments", "commands"))
    )

    doBasicCompletionTest("""
      remote_state {
        backend = ""
        config = {}
        <caret>
      }
    """.trimIndent(), Matcher.and(
      Matcher.all("generate", "disable_init", "disable_dependency_optimization", "encryption"),
      Matcher.not("backend", "config"))
    )

    // Should be no completion, because unit and stack are allowed in terragrunt.stack.hcl only
    doBasicCompletionTest("unit { <caret> }")
    doBasicCompletionTest("stack { <caret> }", Matcher.not("source", "path"))
  }

  fun testStackPropertiesCompletion() {
    val file = myFixture.configureByText(TERRAGRUNT_STACK_FILE, "unit \"some_unit\" { <caret> }")
    myFixture.testCompletionVariants(file.virtualFile.name, "source", "path", "values", "no_dot_terragrunt_stack", "no_validation")

    val file2 = myFixture.configureByText(TERRAGRUNT_STACK_FILE, "errors { <caret> }")
    val completionVariants = myFixture.getCompletionVariants(file2.virtualFile.name) ?: emptyList()
    assertEmpty(completionVariants)
  }

  fun testPropertyPredefinedValuesCompletion() {
    doBasicCompletionTest("""
      remote_state {
        disable_init = <caret>
      }
    """.trimIndent(), "true", "false")

    doBasicCompletionTest("""
      include "some_include_block" {
        path           = ""
        merge_strategy = "<caret>"
      }
    """.trimIndent(), Matcher.and(
      Matcher.all("no_merge", "shallow", "deep"),
      Matcher.not("expose"))
    )

    doBasicCompletionTest("""
      generate "gen_block" {
        if_exists = "over<caret>"
      }
    """.trimIndent(), "overwrite", "overwrite_terragrunt")
  }

  fun testTerragruntFunctionsCompletion() {
    doBasicCompletionTest("""
      remote_state {
        backend = "s3"
        config = {
          bucket = get_aws<caret>
        }
      }
    """.trimIndent(), "get_aws_account_alias", "get_aws_account_id", "get_aws_caller_identity_arn", "get_aws_caller_identity_user_id")
    val allTerragruntFunctions = TerragruntFunctions.map { it.name }
    doBasicCompletionTest("""
      inputs = {
        caller_arn = <caret>
      }
    """.trimIndent(), getPartialMatcher(allTerragruntFunctions))

    doBasicCompletionTest("""
      remote_state {
        backend = "s3"
        config = {
          bucket = <caret>
        }
      }
    """.trimIndent(), Matcher.not(
      "provider::aws::arn_parse", "provider::azurerm::parse_resource_id", "provider::kubernetes::manifest_decode")
    )
    doBasicCompletionTest("include \"root\" {\n  <caret>path = find_in_parent_folders(\"root.hcl\")\n}", emptyList())
  }

  private fun doAutoInsertCompletionTest(textBefore: String, textAfter: String, file: String = this.fileName) {
    myFixture.configureByText(file, textBefore)
    val variants = myFixture.completeBasic()
    assertNull(variants)

    timeoutRunBlocking {
      waitUntilAssertSucceeds("Cannot add required properties to the import block") {
        myFixture.checkResult(textAfter)
      }
    }
  }

  companion object {
    val TerragruntBlockKeywords: List<String> = TerragruntBlocksAndAttributes.map { it.name }

    val StackBlockKeywords: List<String> = StackRootBlocks.map { it.literal }
  }
}