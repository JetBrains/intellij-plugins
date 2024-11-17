// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.testFramework.runInEdtAndWait

class PrettierConfigurationTest : PrettierConfigurationTestBase() {

  fun testRootDirectoryAuto() = doTest(expectedMode = PrettierConfiguration.ConfigurationMode.AUTOMATIC)

  fun testRootDirectoryAutoPackageJson() = doTest(expectedMode = PrettierConfiguration.ConfigurationMode.AUTOMATIC)

  fun testRootDirectoryDisabled() = doTest(expectedMode = PrettierConfiguration.ConfigurationMode.DISABLED)

  fun testSubPackageAuto() = doTest(expectedMode = PrettierConfiguration.ConfigurationMode.AUTOMATIC)

  fun testSubPackageMultipleAuto() = doTest(expectedMode = PrettierConfiguration.ConfigurationMode.AUTOMATIC)

  fun testSubPackageMultipleAutoMixed() = doTest(expectedMode = PrettierConfiguration.ConfigurationMode.AUTOMATIC)

  fun testSubPackageDisabled() = doTest(expectedMode = PrettierConfiguration.ConfigurationMode.DISABLED)

  fun testSubPackageDisabledNoPrettier() = doTest(expectedMode = PrettierConfiguration.ConfigurationMode.DISABLED)

  private fun doTest(expectedMode: PrettierConfiguration.ConfigurationMode) {
    configureAndRun()
    val service = PrettierConfiguration.getInstance(myFixture.project)

    runInEdtAndWait {
      assertEquals(expectedMode, service.configurationMode)
    }
  }
}