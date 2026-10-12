package org.intellij.terraform.config.hints

import com.intellij.codeInsight.hints.VcsCodeVisionProvider
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightModuleFixture
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.utils.codeVision.CodeVisionTestUtil
import com.intellij.testFramework.utils.codeVision.CodeVisionTestUtil.AUTHOR_HINT
import org.intellij.terraform.stack.component.TF_COMPONENT_EXTENSION
import org.intellij.terraform.stack.deployment.TF_DEPLOY_EXTENSION
import org.intellij.terraform.terragrunt.TERRAGRUNT_MAIN_FILE
import org.intellij.terraform.terragrunt.TERRAGRUNT_STACK_FILE
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestApplication
internal class TfVcsCodeVisionTest {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)
  }

  private val myFixture by codeInsightModuleFixture(projectFixture)

  @BeforeEach
  fun setUp() {
    CodeVisionTestUtil.setUp(myFixture)
  }

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

  @Test
  fun testTfAuthorInlayHints(): Unit = testAuthorHints(simpleExample, "main.tf")

  @Test
  fun testTofuAuthorInlayHints(): Unit = testAuthorHints(simpleExample, "main.tofu")

  @Test
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

  @Test
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
    CodeVisionTestUtil.testProviders(myFixture, text, fileName, VcsCodeVisionProvider().groupId)
  }
}
