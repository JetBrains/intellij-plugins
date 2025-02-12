// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.codeinsight

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.Lookup
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.common.waitUntilAssertSucceeds
import org.intellij.terraform.config.model.ResourceType
import org.intellij.terraform.hcl.formatter.HclCodeStyleSettings

internal class TfRequiredProvidersCompletionTestCase : TfBaseCompletionTestCase() {

  fun testTerraformBlockCompletionRequiredProvidersEnabled() {
    performBlockCompletionRequiredProviders("main.tf", """ 
          resource "ad_group<caret>" "" {}
          """.trimIndent(), """ 
                            terraform {
                              required_providers {
                                ad = {
                                  source  = "hanneshayashi/ad"
                                  version = "0.5.4"
                                }
                              }
                            }
                            resource "ad_group" "" {}
                            """.trimIndent(), true)
  }

  fun testTerraformBlockCompletionRequiredProvidersDisabled() {
    performBlockCompletionRequiredProviders("main.tf", """ 
          resource "ad_group<caret>" "" {}
          """.trimIndent(), """ 
                  resource "ad_group" "" {}
                  """.trimIndent(), false)
  }

  fun testTofuBlockCompletionRequiredProvidersEnabled() {
    performBlockCompletionRequiredProviders("main.tofu", """ 
          resource "ad_group<caret>" "" {}
          """.trimIndent(), """ 
                            terraform {
                              required_providers {
                                ad = {
                                  source  = "hanneshayashi/ad"
                                  version = "0.5.4"
                                }
                              }
                            }
                            resource "ad_group" "" {}
                            """.trimIndent(), true)
  }

  fun testTofuBlockCompletionRequiredProvidersDisabled() {
    performBlockCompletionRequiredProviders("main.tofu", """ 
          resource "ad_group<caret>" "" {}
          """.trimIndent(), """ 
                  resource "ad_group" "" {}
                  """.trimIndent(), false)
  }

  fun performBlockCompletionRequiredProviders(fileName: String, initialText: String, finalText: String, importProvidersAutomatically: Boolean) {
    val file = myFixture.configureByText(fileName, initialText)

    val settings = CodeStyle.getCustomSettings(file, HclCodeStyleSettings::class.java)
    settings.IMPORT_PROVIDERS_AUTOMATICALLY = importProvidersAutomatically

    val element = myFixture.complete(CompletionType.BASIC, 2).first { el: LookupElement? ->
      val resourceType = el!!.getObject() as ResourceType
      "hanneshayashi/ad" == resourceType.provider.fullName
    }
    myFixture.lookup.setCurrentItem(element)
    myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR)

    timeoutRunBlocking {
      waitUntilAssertSucceeds("Cannot add required providers block") {
        myFixture.checkResult(finalText)
      }
    }
  }
}