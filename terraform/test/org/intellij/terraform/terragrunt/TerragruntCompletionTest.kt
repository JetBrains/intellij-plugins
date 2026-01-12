// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.terragrunt

import com.intellij.lang.Language
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.common.waitUntilAssertSucceeds
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import junit.framework.TestCase.assertNull
import org.intellij.terraform.config.CompletionTestCase
import org.intellij.terraform.config.codeinsight.TfConfigCompletionTest.Companion.assertNoTfRootBlocks
import org.intellij.terraform.hcl.HCLLanguage
import org.intellij.terraform.stack.TfComponentCompletionTest.Companion.assertNoTfComponentBlocks
import org.intellij.terraform.stack.TfDeployCompletionTest.Companion.assertNoTfDeployBlocks
import org.intellij.terraform.terragrunt.codeinsight.TerragruntUnitHelper
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

  fun testKeywordsCompletionInArray() {
    doBasicCompletionTest("""
      terraform {
        include_in_copy = [<caret>]
      }
    """.trimIndent(), "null", "true", "false")
  }

  fun testNotAllowedRootBlocksInTerragrunt() {
    val file = myFixture.configureByText("test.terragrunt.hcl", "<caret>")
    val completionVariants = myFixture.getCompletionVariants(file.virtualFile.name).orEmpty()
    assertNotEmpty(completionVariants)

    val fileExtension = fileName
    assertNoTfRootBlocks(fileExtension, completionVariants, listOf("terraform", "locals"))
    assertNoTfComponentBlocks(fileExtension, completionVariants, listOf("locals"))
    assertNoTfDeployBlocks(fileExtension, completionVariants)

    assertNoStackBlocks(fileExtension, completionVariants)
  }

  fun testStackRootBlocksCompletion() {
    val file = myFixture.configureByText(TERRAGRUNT_STACK_FILE, "<caret>")
    val completionVariants = myFixture.getCompletionVariants(file.virtualFile.name).orEmpty()

    assertNotEmpty(completionVariants)
    assertEquals(3, completionVariants.size)
    assertContainsElements(completionVariants, "unit", "stack", "locals")
  }

  fun testAutoInsertCompletion() {
    doAutoInsertCompletionTest(myFixture, TERRAGRUNT_MAIN_FILE, "incl<caret>", """
      include "" {
        path = ""
      }
    """.trimIndent())
    doAutoInsertCompletionTest(myFixture, TERRAGRUNT_MAIN_FILE, "gene<caret>", """
      generate "" {
        contents = ""
        path     = ""
      }
    """.trimIndent()
    )

    // Terragrunt Stack files
    doAutoInsertCompletionTest(myFixture, TERRAGRUNT_STACK_FILE, "sta<caret>", """
      stack "" {
        path   = ""
        source = ""
      }
    """.trimIndent())
    doAutoInsertCompletionTest(myFixture, TERRAGRUNT_STACK_FILE, "un<caret>", """
      unit "" {
        path   = ""
        source = ""
      }
    """.trimIndent())
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
    """.trimIndent(), getPartialMatcher(allTerragruntFunctions + TerragruntUnitHelper.TerragruntScope))

    doBasicCompletionTest("""
      remote_state {
        backend = "s3"
        config = {
          bucket = <caret>
        }
      }
    """.trimIndent(), Matcher.not(
      "provider::aws::arn_parse", "provider::azurerm::parse_resource_id", "provider::kubernetes::manifest_decode",
      "unit", "stack")
    )
    doBasicCompletionTest("include \"root\" {\n  <caret>path = find_in_parent_folders(\"root.hcl\")\n}", emptyList())
  }

  companion object {
    val TerragruntBlockKeywords: List<String> = TerragruntBlocksAndAttributes.map { it.name }
    val StackBlockKeywords: List<String> = StackRootBlocks.map { it.literal }

    fun assertNoTerragruntBlocks(
      fileExtension: String,
      completionVariants: List<String>,
      commonRootBlocks: List<String> = emptyList(),
    ) {
      val unexpectedBlocks = TerragruntBlockKeywords.filter { it in completionVariants && it !in commonRootBlocks }
      assertTrue(
        "These Terragrunt-only root blocks should not appear in $fileExtension file: $unexpectedBlocks",
        unexpectedBlocks.isEmpty()
      )
    }

    fun assertNoStackBlocks(
      fileExtension: String,
      completionVariants: List<String>,
      commonRootBlocks: List<String> = listOf("locals"),
    ) {
      val unexpectedBlocks = StackBlockKeywords.filter { it in completionVariants && it !in commonRootBlocks }
      assertTrue(
        "These Stack-only root blocks should not appear in $fileExtension file: $unexpectedBlocks",
        unexpectedBlocks.isEmpty()
      )
    }
  }
}

internal fun doAutoInsertCompletionTest(
  myFixture: CodeInsightTestFixture,
  fileName: String,
  textBefore: String,
  textAfter: String,
) {
  myFixture.configureByText(fileName, textBefore)
  val variants = myFixture.completeBasic()
  assertNull(variants)

  timeoutRunBlocking {
    waitUntilAssertSucceeds("Cannot add required properties to the Hcl block") {
      myFixture.checkResult(textAfter)
    }
  }
}