package org.intellij.terraform.config.actions

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.common.waitUntilAssertSucceeds
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.terraform.TfTestUtils

internal class AddRequiredProvidersBlockTest: BasePlatformTestCase() {

  override fun runInDispatchThread(): Boolean {
    return false
  }

  internal fun getTestClassName(): String {
    return PlatformTestUtil.lowercaseFirstLetter(javaClass.simpleName, true)
  }

  private fun doCompletionWithDelay() {
    val filePath = "${TfTestUtils.getTestDataRelativePath()}/terraform/completion/${getTestClassName()}/${getTestName(true)}"
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
}