// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.terragrunt

import com.intellij.lang.Language
import org.intellij.terraform.config.CompletionTestCase
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.RootBlockKeywords
import org.intellij.terraform.hcl.HCLLanguage
import org.intellij.terraform.terragrunt.model.StackRootBlocks
import org.intellij.terraform.terragrunt.model.TerragruntRootBlocks

internal class TerragruntCompletionTest : CompletionTestCase() {
  override fun getFileName(): String = "terragrunt.hcl"
  override fun getExpectedLanguage(): Language = HCLLanguage

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

  companion object {
    val TerragruntBlockKeywords: List<String> = TerragruntRootBlocks.map { it.literal }
    val StackBlockKeywords: List<String> = StackRootBlocks.map { it.literal }
  }
}