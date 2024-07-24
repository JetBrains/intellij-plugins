// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.documentation

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.terraform.config.model.TypeModelProvider

internal class OfflineDescriptionTestCase: BasePlatformTestCase() {

  fun testOfflineProviderDescriptionExists() {
    val model = TypeModelProvider.globalModel
    myFixture.configureByText("main.tf", """
      terraform {
        required_providers {
          azapi = {
            source  = "Azure/azapi"
            version = "1.13.1"
          }
        }
      }
      provider "az<caret>api" {}
    """.trimIndent())
    val providerType = model.getProviderType("azapi", myFixture.elementAtCaret)
    assertNotNull(providerType?.description)
  }

  fun testOfflineResourceDescriptionExists() {
    val model = TypeModelProvider.globalModel
    myFixture.configureByText("main.tf", """
      terraform {
        required_providers {
          auth0 = {
            source  = "auth0/auth0"
            version = "1.3.0"
          }
        }
      }
      
      resource "auth0<caret>_action" "action" {
        code = ""
        name = ""
      }
    """.trimIndent())
    val providerType = model.getResourceType("auth0_action", myFixture.elementAtCaret)
    assertNotNull(providerType?.description)
  }

  fun testOfflineDatasourceDescriptionExists() {
    val model = TypeModelProvider.globalModel
    myFixture.configureByText("main.tf", """
      terraform {
        required_providers {
          auth0 = {
            source  = "auth0/auth0"
            version = "1.3.0"
          }
        }
      }
      data "auth0_a<caret>ttack_protection" "protection" {
        
      }
    """.trimIndent())
    val providerType = model.getDataSourceType("auth0_attack_protection", myFixture.elementAtCaret)
    assertNotNull(providerType?.description)
  }

}