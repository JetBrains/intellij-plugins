// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.codeinsight

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.Lookup
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.common.waitUntilAssertSucceeds
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.runInEdtAndWait
import org.intellij.terraform.config.actions.waitForImportProviderTasks
import org.intellij.terraform.config.model.ResourceType
import org.intellij.terraform.hcl.formatter.HclCodeStyleSettings

internal class TfRequiredProvidersCompletionTestCase : BasePlatformTestCase() {

  private val resourceBlock = """
    resource "libvirt_cloudinit_disk<caret>" "" {
      meta_data = ""
      name      = ""
      user_data = ""
    }
  """.trimIndent()

  private val resourceBlockWithoutCaret = """
    resource "libvirt_cloudinit_disk" "" {
      meta_data = ""
      name      = ""
      user_data = ""
    }
  """.trimIndent()

  private val requiredProvidersBlock = """
    terraform {
      required_providers {
        libvirt = {
          source  = "dmacvicar/libvirt"
          version = "0.9.4"
        }
      }
    }
  """.trimIndent()

  private fun runCompletionTest(fileName: String, importProvidersAutomatically: Boolean) {
    val initialText = resourceBlock
    val finalText = if (importProvidersAutomatically) {
      "$requiredProvidersBlock\n$resourceBlockWithoutCaret"
    }
    else {
      resourceBlockWithoutCaret
    }

    performBlockCompletionRequiredProviders(fileName, initialText, finalText, importProvidersAutomatically)
  }

  fun testTerraformBlockCompletionRequiredProvidersEnabled() {
    runCompletionTest("main.tf", importProvidersAutomatically = true)
  }

  fun testTerraformBlockCompletionRequiredProvidersDisabled() {
    runCompletionTest("main.tf", importProvidersAutomatically = false)
  }

  fun testTofuBlockCompletionRequiredProvidersEnabled() {
    runCompletionTest("main.tofu", importProvidersAutomatically = true)
  }

  fun testTofuBlockCompletionRequiredProvidersDisabled() {
    runCompletionTest("main.tofu", importProvidersAutomatically = false)
  }

  fun performBlockCompletionRequiredProviders(fileName: String, initialText: String, finalText: String, importProvidersAutomatically: Boolean) {
    val file = myFixture.configureByText(fileName, initialText)

    val settings = CodeStyle.getCustomSettings(file, HclCodeStyleSettings::class.java)
    settings.IMPORT_PROVIDERS_AUTOMATICALLY = importProvidersAutomatically

    runInEdtAndWait {
      val element = myFixture.complete(CompletionType.BASIC, 2).first { el: LookupElement? ->
        val resourceType = el!!.getObject() as ResourceType
        "dmacvicar/libvirt" == resourceType.provider.fullName
      }
      myFixture.lookup.setCurrentItem(element)
      myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR)
    }

    timeoutRunBlocking {
      waitForImportProviderTasks(project)
      waitUntilAssertSucceeds("Cannot add required providers block") {
        myFixture.checkResult(finalText)
      }
    }
  }

  override fun runInDispatchThread(): Boolean = false
}