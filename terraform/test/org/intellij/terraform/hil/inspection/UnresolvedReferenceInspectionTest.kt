// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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

  fun testEphemeralRecourseResolve() {
    myFixture.configureByText("main.tf", """
      ephemeral "azurerm_key_vault_secret" "db_password1" {
        key_vault_id = ""
        name         = ""
      }

      resource "aws_secretsmanager_secret" "db_password" {
        name = "db_password"
      }

      resource "aws_secretsmanager_secret_version" "db_password" {
        secret_id = ephemeral.azurerm_key_vault_secret.db_password1.name
      }

      ephemeral "aws_secretsmanager_secret_version" "db_password2" {
        secret_id = aws_secretsmanager_secret_version.db_password.secret_id
      }

      resource "aws_db_instance" "example" {
        instance_class      = ephemeral.aws_secretsmanager_secret_version.db_password2.secret_string
        allocated_storage   = "5"
        engine              = "postgres"
        username            = "example"
        skip_final_snapshot = true
      }
    """.trimIndent())

    myFixture.enableInspections(HILUnresolvedReferenceInspection())
    myFixture.checkHighlighting()
  }
}