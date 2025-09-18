// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.hint

import com.intellij.codeInsight.codeVision.CodeVisionAnchorKind
import com.intellij.codeInsight.codeVision.settings.CodeVisionSettings
import com.intellij.openapi.options.advanced.AdvancedSettings
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.utils.codeVision.CodeVisionTestCase
import org.intellij.terraform.config.hints.TF_USAGES_LIMIT_ID
import org.intellij.terraform.config.hints.TfReferencesCodeVisionProvider

internal class TfCodeVisionProviderTest : CodeVisionTestCase() {

  private val usagesLimit = 4

  override fun setUp() {
    super.setUp()
    CodeVisionSettings.getInstance().defaultPosition = CodeVisionAnchorKind.Right
    setUpAdvancedSettings()
  }

  fun testHintsForVariousBlocks() = testUsageHints($$"""
    provider "aws" {
      region = "us-west-2"
    }

    variable "instances" {/*<# [3 usages] #>*/
      type = map(object({
        ami           = string
        instance_type = string
      }))
      default = {
        instance1 = {
          ami           = "ami-0c55b159cbfafe1f0"
          instance_type = "t2.micro"
        }
        instance2 = {
          ami           = "ami-0c55b159cbfafe1f0"
          instance_type = "t2.small"
        }
        instance3 = {
          ami           = "ami-0c55b159cbfafe1f0"
          instance_type = "t3.micro"
        }
      }
    }

    variable "tag_name" {/*<# [no usages] #>*/
      type    = string
      default = "ExampleInstance"
    }

    locals {
      non_using      = ""/*<# [no usages] #>*/
      instance_count = length(var.instances)/*<# [1 usage] #>*/
      instance_amis  = [for instance in var.instances : instance.ami]/*<# [no usages] #>*/
      tag_suffix     = "env"/*<# [1 usage] #>*/
    }

    resource "aws_instance" "example" {/*<# [1 usage] #>*/
      for_each = var.instances

      ami           = each.value.ami
      instance_type = each.value.instance_type

      tags = {
        Name = "${each.key}-${local.tag_suffix}"
      }
    }

    output "instance_ids" {
      value = { for key, instance in aws_instance.example : key => instance.id }
    }

    output "total_instances" {
      value = local.instance_count
    }
    
    data "aws_ami" "latest_amazon_linux" {/*<# [2 usages] #>*/
      most_recent = true

      filter {
        name   = "name"
        values = ["amzn2-ami-hvm-*-x86_64-gp2"]
      }

      filter {
        name   = "owner-alias"
        values = ["amazon"]
      }

      owners = ["amazon"]
    }

    data "aws_vpc" "default" {/*<# [no usages] #>*/
      default = true
    }

    resource "aws_instance" "test" {/*<# [no usages] #>*/
      ami           = data.aws_ami.latest_amazon_linux.id
      instance_type = "t2.micro"
      subnet_id     = data.aws_ami.latest_amazon_linux.tpm_support

      tags = {
        Name = "ExampleInstance"
      }
    }
  """.trimIndent()
  )

  fun testCodeVisionLimit() = testUsageHints($$"""
    variable "region" {/*<# [4+ usages] #>*/
      description = "AWS region to deploy resources in"
      type        = string
      default     = "us-east-1"
    }

    provider "aws" {
      region = var.region
    }

    resource "aws_s3_bucket" "bucket1" {/*<# [no usages] #>*/
      bucket = "example-bucket-1-${var.region}"
    }

    resource "aws_s3_bucket" "bucket2" {/*<# [no usages] #>*/
      bucket = "example-bucket-2-${var.region}"
    }

    resource "aws_s3_bucket" "bucket3" {/*<# [no usages] #>*/
      bucket = "example-bucket-3-${var.region}"
    }

    resource "aws_s3_bucket" "bucket4" {/*<# [no usages] #>*/
      bucket = "example-bucket-4-${var.region}"
    }
  """.trimIndent()
  )

  private fun testUsageHints(text: String) {
    testProviders(text, "main.tf", TfReferencesCodeVisionProvider().groupId)
  }

  private fun setUpAdvancedSettings() {
    val prevValue = AdvancedSettings.getInt(TF_USAGES_LIMIT_ID)
    AdvancedSettings.setInt(TF_USAGES_LIMIT_ID, usagesLimit)

    Disposer.register(testRootDisposable) {
      AdvancedSettings.setInt(TF_USAGES_LIMIT_ID, prevValue)
    }
  }
}