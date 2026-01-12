// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.stack

import com.intellij.lang.Language
import org.intellij.terraform.config.CompletionTestCase
import org.intellij.terraform.config.codeinsight.TfConfigCompletionTest.Companion.assertNoTfRootBlocks
import org.intellij.terraform.hcl.HCLLanguage
import org.intellij.terraform.stack.TfComponentCompletionTest.Companion.assertNoTfComponentBlocks
import org.intellij.terraform.stack.deployment.TF_DEPLOY_EXTENSION
import org.intellij.terraform.stack.deployment.TfDeployRootBlocks
import org.intellij.terraform.terragrunt.TerragruntCompletionTest.Companion.assertNoStackBlocks
import org.intellij.terraform.terragrunt.TerragruntCompletionTest.Companion.assertNoTerragruntBlocks
import org.intellij.terraform.terragrunt.doAutoInsertCompletionTest

internal class TfDeployCompletionTest : CompletionTestCase() {

  override fun getFileName(): String = "example.$TF_DEPLOY_EXTENSION"
  override fun getExpectedLanguage(): Language = HCLLanguage
  override fun runInDispatchThread(): Boolean = false

  fun testTfDeployRootBlocksCompletion() {
    doBasicCompletionTest("<caret> ", TfDeployBlockKeywords)
    doBasicCompletionTest("<caret> {}", TfDeployBlockKeywords)
    doBasicCompletionTest("""
      deployment "some_deploy" {
        inputs = {}
      }
      <caret>
    """.trimIndent(), TfDeployBlockKeywords)
    doBasicCompletionTest("""
      deployment "some_deploy" {
        inputs = {}
      }
      <caret> {}
    """.trimIndent(), TfDeployBlockKeywords)
  }

  fun testNotAllowedRootBlocksInTfDeploy() {
    val file = myFixture.configureByText(fileName, "<caret>")
    val completionVariants = myFixture.getCompletionVariants(file.virtualFile.name).orEmpty()
    assertNotEmpty(completionVariants)

    val fileExtension = TF_DEPLOY_EXTENSION
    val commonRootBlocks = listOf("locals")
    assertNoTfRootBlocks(fileExtension, completionVariants, commonRootBlocks)
    assertNoTfComponentBlocks(fileExtension, completionVariants, commonRootBlocks)

    assertNoTerragruntBlocks(fileExtension, completionVariants, commonRootBlocks)
    assertNoStackBlocks(fileExtension, completionVariants, commonRootBlocks)
  }

  fun testAutoInsertCompletion() {
    doAutoInsertCompletionTest(myFixture, fileName, "iden<caret>", """
      identity_token "" {
        audience = []
      }
    """.trimIndent())

    doAutoInsertCompletionTest(myFixture, fileName, "deployment_auto<caret>", """
      deployment_auto_approve "" {
        check {
          condition = false
          reason    = ""
        }
      }
    """.trimIndent())
  }

  fun testPropertiesCompletion() {
    doBasicCompletionTest("""
      deployment "test" {
        inputs = {}
        de<caret>
      }
    """.trimIndent(), 2, "destroy", "deployment_group")

    doBasicCompletionTest("""
      upstream_input "" {
        source = ""
        <caret> = ""
      }
    """.trimIndent(), 1, "type")

    doBasicCompletionTest("deployment_auto_approve \"test\" { check { <caret> } }", 2, "condition", "reason")
  }

  companion object {
    val TfDeployBlockKeywords: List<String> = TfDeployRootBlocks.map { it.name }

    fun assertNoTfDeployBlocks(
      fileExtension: String,
      completionVariants: List<String>,
      commonRootBlocks: List<String> = listOf("locals"),
    ) {
      val unexpectedBlocks = TfDeployBlockKeywords.filter { it in completionVariants && it !in commonRootBlocks }
      assertTrue(
        "These Tf Deploy-only root blocks should not appear in $fileExtension file: $unexpectedBlocks",
        unexpectedBlocks.isEmpty()
      )
    }
  }
}
