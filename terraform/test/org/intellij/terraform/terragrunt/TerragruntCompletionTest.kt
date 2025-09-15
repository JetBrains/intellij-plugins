// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.terragrunt

import com.intellij.lang.Language
import org.intellij.terraform.config.CompletionTestCase
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.RootBlockKeywords
import org.intellij.terraform.hcl.HCLLanguage

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

  fun testStackRootBlocksCompletion() {
    val file = myFixture.configureByText("terragrunt.stack.hcl", "<caret>")
    val completionVariants = myFixture.getCompletionVariants(file.virtualFile.name).orEmpty()

    assertNotEmpty(completionVariants)
    assertEquals(2, completionVariants.size)
    assertContainsElements(completionVariants, "unit", "stack")
  }
}