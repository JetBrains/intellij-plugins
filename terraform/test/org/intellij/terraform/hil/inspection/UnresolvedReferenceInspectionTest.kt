// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.inspection

import com.intellij.testFramework.fixtures.BasePlatformTestCase

internal class UnresolvedReferenceInspectionTest: BasePlatformTestCase() {

  fun testSimpleReference(){
    myFixture.configureByText("vars.tf", """
      provider "aws" {
        region = var.aws_region
      }
      
      variable "aws_region" {
        description = "The AWS region to deploy resources in."
        type        = string
        default     = "us-west-2"
      }
      
      output "aws_region_used" {
        description = "The AWS region where resources are deployed."
        value       = var.aws_region
      }      
    """.trimIndent())
    myFixture.enableInspections(HILUnresolvedReferenceInspection())
    myFixture.checkHighlighting()
  }

  fun testWrongReference(){
    myFixture.configureByText("vars.tf", """
      provider "aws" {
        region = var.<error descr="Unresolved reference aws_region">aws_region</error>
      }
      
      variable "aws_region_wrong" {
        description = "The AWS region to deploy resources in."
        type        = string
        default     = "us-west-2"
      }
      
      output "aws_region_used" {
        description = "The AWS region where resources are deployed."
        value       = var.<error descr="Unresolved reference aws_region">aws_region</error>
      }      
    """.trimIndent())
    myFixture.enableInspections(HILUnresolvedReferenceInspection())
    myFixture.checkHighlighting()
  }


  fun testSkipPackerFiles(){
    myFixture.configureByText("image.pkr.hcl", """
      variable "ssh_username" {
        default = "Administrator"
      }
      variable "ssh_host" {
        type = string
      }
      variable "ssh_domain" {
        type = string
      }
      source null null {
        communicator = "ssh"

        ssh_host       = "$\{var.ssh_host}.$\{var.ssh_domain}"
        ssh_username   = var.ssh_username
        ssh_timeout    = "1m"
        ssh_agent_auth = true
      }
    """.trimIndent())
    myFixture.enableInspections(HILUnresolvedReferenceInspection())
    myFixture.checkHighlighting()
  }

}