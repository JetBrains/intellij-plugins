// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.actions

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.openapi.application.runReadAction
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.assertEqualsToFile
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.common.waitUntilAssertSucceeds
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.runInEdtAndWait
import org.intellij.terraform.TfTestUtils
import java.io.File

internal class TfAddRequiredProvidersBlockTest : BasePlatformTestCase() {
  override fun getTestDataPath(): String = "${TfTestUtils.getTestDataPath()}/terraform/addRequiredProviders"

  override fun runInDispatchThread(): Boolean {
    return false
  }

  private fun doCompletionWithDelay() {
    val filePath = "${getTestName(true)}/main"
    myFixture.configureByFile("$filePath.tf")
    val variants = myFixture.complete(CompletionType.BASIC, 2)
    assertNull(variants)
    timeoutRunBlocking {
      waitUntilAssertSucceeds("Cannot complete variants asynchronously for test file: $filePath") {
        myFixture.checkResultByFile("$filePath.after.tf")
      }
    }
  }

  fun testAddProviderToEmptyFile() {
    doCompletionWithDelay()
  }

  fun testAddResourceToEmptyFile() {
    doCompletionWithDelay()
  }

  fun testAddProviderToEmptyTerraformBlock() {
    doCompletionWithDelay()
  }

  fun testAddProviderToExistingProvidersList() {
    doCompletionWithDelay()
  }

  fun testAddProviderForEphemeralBlock() {
    doCompletionWithDelay()
  }

  fun testAddProviderToAnotherFile() {
    val filePaths = listOf("main", "providers").map { "$testDataPath/${getTestName(true)}/$it" }

    val psiFiles = myFixture.configureByFiles(*filePaths.map { "$it.tf" }.toTypedArray())
    val variants = myFixture.complete(CompletionType.BASIC, 2)
    assertNull(variants)

    runInEdtAndWait {
      PsiDocumentManager.getInstance(project).commitAllDocuments()
    }

    runReadAction {
      psiFiles.forEachIndexed { index, psiFile ->
        val expectedFile = File("${filePaths[index]}.after.tf")
        assertEqualsToFile(
          "File ${psiFile.name} does not match expected content",
          expectedFile,
          psiFile.text.trim()
        )
      }
    }
  }
}