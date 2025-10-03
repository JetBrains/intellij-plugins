// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.terragrunt

import com.intellij.lang.Language
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.common.waitUntilAssertSucceeds
import org.intellij.terraform.config.CompletionTestCase
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.RootBlockKeywords
import org.intellij.terraform.hcl.HCLLanguage

internal class TerragruntCompletionTest : CompletionTestCase() {
  override fun getFileName(): String = "terragrunt.hcl"
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
    val file = myFixture.configureByText(TERRAGRUNT_STACK_EXTENSION, "<caret>")
    val completionVariants = myFixture.getCompletionVariants(file.virtualFile.name).orEmpty()

    assertNotEmpty(completionVariants)
    assertEquals(2, completionVariants.size)
    assertContainsElements(completionVariants, "unit", "stack")
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
    """.trimIndent(), TERRAGRUNT_STACK_EXTENSION)
    doAutoInsertCompletionTest("un<caret>", """
      unit "" {
        path   = ""
        source = ""
      }
    """.trimIndent(), TERRAGRUNT_STACK_EXTENSION)
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
    val file = myFixture.configureByText(TERRAGRUNT_STACK_EXTENSION, "unit \"some_unit\" { <caret> }")
    myFixture.testCompletionVariants(file.virtualFile.name, "source", "path", "values", "no_dot_terragrunt_stack", "no_validation")

    val file2 = myFixture.configureByText(TERRAGRUNT_STACK_EXTENSION, "errors { <caret> }")
    val completionVariants = myFixture.getCompletionVariants(file2.virtualFile.name) ?: emptyList()
    assertEmpty(completionVariants)
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
}