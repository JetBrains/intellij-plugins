package org.intellij.terraform.config.hint

import com.intellij.testFramework.utils.codeVision.CodeVisionTestCase
import org.intellij.terraform.config.hints.TfReferencesCodeVisionProvider

class TfCodeVisionProviderTest : CodeVisionTestCase() {

  fun testInheritors() = doTest("""
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

    resource "aws_instance" "example" {
      for_each = var.instances

      ami           = each.value.ami
      instance_type = each.value.instance_type

      tags = {
        Name = "${Dollar}{each.key}-${Dollar}{local.tag_suffix}"
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

    resource "aws_instance" "test" {
      ami           = data.aws_ami.latest_amazon_linux.id
      instance_type = "t2.micro"
      subnet_id     = data.aws_ami.latest_amazon_linux.tpm_support

      tags = {
        Name = "ExampleInstance"
      }
    }
  """.trimIndent(), TfReferencesCodeVisionProvider().groupId)

  private fun doTest(text: String, vararg enabledProviderGroupIds: String) {
    testProviders(text, "main.tf", *enabledProviderGroupIds)
  }
}

private const val Dollar = '$'