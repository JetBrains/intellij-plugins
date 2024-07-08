package org.intellij.terraform.config.actions

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.util.parentOfType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.terraform.config.codeinsight.InsertHandlerService
import org.intellij.terraform.config.codeinsight.TfModelHelper
import org.intellij.terraform.config.model.ProviderType
import org.intellij.terraform.config.model.getProviderForBlockType
import org.intellij.terraform.hcl.psi.HCLBlock

internal class AddRequiredProvidersBlockTest: BasePlatformTestCase() {

  fun testAddProviderToEmptyFile() {
    myFixture.configureByText("main.tf", """
      provider "tsuru<caret>" {

      }
    """.trimIndent())
    WriteCommandAction.writeCommandAction(project).run<Throwable> {
      InsertHandlerService.getInstance(project).addRequiredProvidersBlockToConfig(getProviderType(), myFixture.file)
    }
    myFixture.checkResult("""
      terraform {
        required_providers {
          tsuru = {
            source  = "tsuru/tsuru"
            version = "2.12.0"
          }
        }
      }
      provider "tsuru" {

      }
    """.trimIndent())
  }

  fun testAddResourceToEmptyFile() {
    myFixture.configureByText("main.tf", """
      resource "aci_cloud_ad<caret>" "cloud_ad" {
        active_directory_id = "ad_id"
        tenant_dn           = "id1"
      }
    """.trimIndent())
    WriteCommandAction.writeCommandAction(project).run<Throwable> {
      InsertHandlerService.getInstance(project).addRequiredProvidersBlockToConfig(getProviderType(), myFixture.file)
    }
    myFixture.checkResult("""
      terraform {
        required_providers {
          aci = {
            source  = "CiscoDevNet/aci"
            version = "2.14.0"
          }
        }
      }
      resource "aci_cloud_ad" "cloud_ad" {
        active_directory_id = "ad_id"
        tenant_dn           = "id1"
      }
    """.trimIndent())
  }

  fun testAddProviderToEmptyTerraformBlock() {
    myFixture.configureByText("main.tf", """
      terraform {
        required_version = "1.1.3"
      }
      resource "abbey_demo<caret>" "demo" {
        email      = "a@a.a"
        permission = "user"
      }
    """.trimIndent())
    WriteCommandAction.writeCommandAction(project).run<Throwable> {
      InsertHandlerService.getInstance(project).addRequiredProvidersBlockToConfig(getProviderType(), myFixture.file)
    }
    myFixture.checkResult("""
      terraform {
        required_version = "1.1.3"
        required_providers {
          abbey = {
            source  = "abbeylabs/abbey"
            version = "0.2.9"
          }
        }
      }
      resource "abbey_demo" "demo" {
        email      = "a@a.a"
        permission = "user"
      }
    """.trimIndent())
  }

  fun testAddProviderToExistingProvidersList() {
    myFixture.configureByText("main.tf", """
      terraform {
        required_providers {
          aws-sso-scim = {
            source  = "burdaforward/aws-sso-scim"
            version = ">= 0.1.0"
          }
        }
      }

      resource "aws-sso-scim_user" "user" {
        display_name = "John Doe"
        family_name  = "Doe"
        given_name   = "John"
        user_name    = "jdoe"
      }

      resource "ably_app<caret>" "ably" {
        name = "app"
      }
    """.trimIndent())
    WriteCommandAction.writeCommandAction(project).run<Throwable> {
      InsertHandlerService.getInstance(project).addRequiredProvidersBlockToConfig(getProviderType(), myFixture.file)
    }
    myFixture.checkResult("""
      terraform {
        required_providers {
          aws-sso-scim = {
            source  = "burdaforward/aws-sso-scim"
            version = ">= 0.1.0"
          }
          ably = {
            source  = "ably/ably"
            version = "0.6.1"
          }
        }
      }

      resource "aws-sso-scim_user" "user" {
        display_name = "John Doe"
        family_name  = "Doe"
        given_name   = "John"
        user_name    = "jdoe"
      }

      resource "ably_app" "ably" {
        name = "app"
      }
    """.trimIndent())
  }



  private fun getProviderType(): ProviderType {
    val block = myFixture.elementAtCaret.parentOfType<HCLBlock>()!!
    val blockTypes = TfModelHelper.getAllTypesForBlockByIdentifier(block)
    assertEquals(1, blockTypes.size)
    val providerType = getProviderForBlockType(blockTypes.first())!!
    return providerType
  }

}