package org.intellij.terraform.config.hints

import com.intellij.codeInsight.hints.VcsCodeVisionProvider
import com.intellij.testFramework.utils.codeVision.CodeVisionTestCase
import org.intellij.terraform.stack.component.TF_COMPONENT_EXTENSION
import org.intellij.terraform.stack.deployment.TF_DEPLOY_EXTENSION
import org.intellij.terraform.terragrunt.TERRAGRUNT_MAIN_FILE
import org.intellij.terraform.terragrunt.TERRAGRUNT_STACK_FILE

internal class TfVcsCodeVisionTest : CodeVisionTestCase() {
  private val simpleExample = """
    terraform {/*<# [$AUTHOR_HINT] #>*/
      required_providers {
        aws = {
          source  = "hashicorp/aws"
          version = ">= 6.0.0"
        }
      }
    }

    provider "aws" {/*<# [$AUTHOR_HINT] #>*/
      region = "us-east-1"
    }
  """.trimIndent()

  fun testTfAuthorInlayHints() = testAuthorHints(simpleExample, "main.tf")

  fun testTofuAuthorInlayHints() = testAuthorHints(simpleExample, "main.tofu")

  fun testTerragruntAuthorInlayHints() {
    testAuthorHints("""
      terraform {/*<# [$AUTHOR_HINT] #>*/
        source = "git::https://github.com/hashicorp/terraform-aws-modules//vpc?ref=v3.14.2"
      }
      
      locals {/*<# [$AUTHOR_HINT] #>*/
        name = "example-vpc"
        cidr = "10.0.0.0/16"
      }
    """.trimIndent(), "test.$TERRAGRUNT_MAIN_FILE")

    testAuthorHints("""
      locals { }/*<# [$AUTHOR_HINT] #>*/
      
      unit "service" {/*<# [$AUTHOR_HINT] #>*/
        source = "../units/service"
        path   = "service"
      }
    """.trimIndent(), "test.$TERRAGRUNT_STACK_FILE")
  }

  fun testTfStackAuthorInlayHints() {
    testAuthorHints("""
      required_providers {/*<# [$AUTHOR_HINT] #>*/
        aws = {
          source  = "hashicorp/aws"
          version = ">= 5.0.0"
        }
      }
    """.trimIndent(), "test.$TF_COMPONENT_EXTENSION")

    testAuthorHints("""
      deployment "development" {/*<# [$AUTHOR_HINT] #>*/
        inputs = {
          aws_region = "us-east-1"
          env        = "dev"
        }
      }""".trimIndent(), "test.$TF_DEPLOY_EXTENSION")
  }

  private fun testAuthorHints(text: String, fileName: String) {
    testProviders(text, fileName, VcsCodeVisionProvider().groupId)
  }
}
