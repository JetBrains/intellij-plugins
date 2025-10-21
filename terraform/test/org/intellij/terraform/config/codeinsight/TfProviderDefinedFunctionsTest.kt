// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.codeinsight

import com.intellij.psi.util.childrenOfType
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLDefinedMethodExpression
import org.intellij.terraform.hcl.psi.getNameElementUnquoted

internal class TfProviderDefinedFunctionsTest : TfBaseCompletionTestCase() {
  fun testProviderDefinedFunctionsCompletion() {
    doBasicCompletionTest(
      """
        resource "kubernetes_manifest" "example" {
          manifest = manifest_d<caret>
        }
      """.trimIndent(), 2, "provider::kubernetes::manifest_decode", "provider::kubernetes::manifest_decode_multi"
    )

    doBasicCompletionTest(
      """
        locals {
          tfvars = decode_tf<caret>
        }
        """.trimIndent(), 1, "provider::terraform::decode_tfvars"
    )
  }

  fun testTypeOfProviderDefinedFunctions() {
    doBasicCompletionTest(
      """
        resource "aws_ecr_repository" "hashicups" {
          name = "hashicups"
        
          image_scanning_configuration {
            scan_on_push = true
          }
        }
        
        output "hashicups_ecr_repository_account_id" {
          value = provider::aws::arn_parse(aws_ecr_repository.hashicups.arn).<caret>
        }
        """.trimIndent(), 0
    )

    doBasicCompletionTest(
      """
        output "example" {
          value = provider::aws::trim_iam_role_path("arn:aws:iam::444455556666:role/with/path/example").<caret>
        }
        """.trimIndent(), 0
    )
  }

  fun testIncorrectProviderDefinedFunction() {
    myFixture.configureByText("main.tf", getIncorrectProviderFunctionText())
    val file = myFixture.file
    assertNotNull(file)

    val outputBlocks = file.childrenOfType<HCLBlock>().filter { TfPsiPatterns.OutputRootBlock.accepts(it) }
    assertEquals(1, outputBlocks.size)
    val providerFunction = outputBlocks.first().`object`?.findProperty("value")?.value
    assertTrue(providerFunction is HCLDefinedMethodExpression)

    val resourceBlocks = file.childrenOfType<HCLBlock>().filter { TfPsiPatterns.ResourceRootBlock.accepts(it) }
    assertEquals(1, resourceBlocks.size)
    assertEquals("aws_instance", resourceBlocks.first().getNameElementUnquoted(1))

    myFixture.configureByText("main.tf", getIncorrectProviderFunctionText(withHighlightingTag = true))
    myFixture.checkHighlighting(true, false, true)
  }

  private fun getIncorrectProviderFunctionText(withHighlightingTag: Boolean = false): String {
    val providerFunctionText = if (withHighlightingTag) "<error descr=\"Expected keyword 'provider' at the beginning of the provider-defined function\">provide</error>" else "provide"
    return """
      output "example" {
        value = ${providerFunctionText}::aws::arn_build("aws", "iam", "", "444455556666", "role/example")
      }

      resource "aws_instance" "example" {
        ami           = "ami-123456"
        instance_type = "t2.micro"
      }
    """.trimIndent()
  }
}