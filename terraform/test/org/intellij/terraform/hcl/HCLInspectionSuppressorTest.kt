// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.terraform.hil.inspection.HILUnresolvedReferenceInspection

class HCLInspectionSuppressorTest : BasePlatformTestCase() {
  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(HILUnresolvedReferenceInspection::class.java)
  }

  fun testInspectionSuppressedProperty() {
    myFixture.configureByText("main.tf", """
      provider "aws" {
        region = "us-east-1"
      }

      resource "aws_instance" "example" {
        ami           = "ami-12345678"
        # noinspection HILUnresolvedReference
        instance_type = var.unresolved_property 
      }
    """.trimIndent()
    )
    myFixture.checkHighlighting()
  }

  fun testInspectionSuppressedBlock() {
    myFixture.configureByText("main.tf", """
       # noinspection HILUnresolvedReference
      data "aws_servicequotas_service_quota" "managed_by_name" {
        for_each = local.quotas_to_manage_by_name

        service_code = each.value.service_code
        quota_name   = each.value.quota_name
      }
    """.trimIndent())
    myFixture.checkHighlighting()
  }

  fun testSuppressUnresolvedProperty() {
    myFixture.configureByText("main.tf", """
      resource "aws_servicequotas_service_quota" "managed_by_code" {
        for_each = local.quotas_to_manage_by_code

        quota_code   = each.value.<caret>quota_code
        service_code = each.value.service_code
        value        = "test"
      }
    """.trimIndent())
    val intention = myFixture.findSingleIntention("Suppress for property")
    myFixture.launchAction(intention)
    myFixture.checkResult("""
      resource "aws_servicequotas_service_quota" "managed_by_code" {
        for_each = local.quotas_to_manage_by_code

        # noinspection HILUnresolvedReference
        quota_code   = each.value.quota_code
        service_code = each.value.service_code
        value        = "test"
      }
    """.trimIndent())
  }

  fun testSuppressUnresolvedBlock() {
    myFixture.configureByText("main.tf", """
      resource "aws_servicequotas_service_quota" "managed_by_code" {
        for_each = local.quotas_to_manage_by_code

        quota_code   = each.value.<caret>quota_code
        service_code = each.value.service_code
        value        = "test"
      }
    """.trimIndent())
    val intention = myFixture.findSingleIntention("Suppress for block")
    myFixture.launchAction(intention)
    myFixture.checkResult("""
      # noinspection HILUnresolvedReference
      resource "aws_servicequotas_service_quota" "managed_by_code" {
        for_each = local.quotas_to_manage_by_code

        quota_code   = each.value.quota_code
        service_code = each.value.service_code
        value        = "test"
      }
    """.trimIndent())
  }
}