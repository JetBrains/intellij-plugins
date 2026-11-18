// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.unit

import com.intellij.prettierjs.PrettierConfiguration
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.runInEdtAndWait
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@TestApplication
class PrettierConfigurationTest {
  private val prettier by prettierConfigurationFixture()

  @Test
  fun testRootDirectoryAuto() = doTest(expectedMode = PrettierConfiguration.ConfigurationMode.AUTOMATIC)

  @Test
  fun testRootDirectoryAutoPackageJson() = doTest(expectedMode = PrettierConfiguration.ConfigurationMode.AUTOMATIC)

  @Test
  fun testRootDirectoryDisabled() = doTest(expectedMode = PrettierConfiguration.ConfigurationMode.DISABLED)

  @Test
  fun testSubPackageAuto() = doTest(expectedMode = PrettierConfiguration.ConfigurationMode.AUTOMATIC)

  @Test
  fun testSubPackageMultipleAuto() = doTest(expectedMode = PrettierConfiguration.ConfigurationMode.AUTOMATIC)

  @Test
  fun testSubPackageMultipleAutoMixed() = doTest(expectedMode = PrettierConfiguration.ConfigurationMode.AUTOMATIC)

  @Test
  fun testRootDependencyNestedConfig() = doTest(expectedMode = PrettierConfiguration.ConfigurationMode.AUTOMATIC)

  @Test
  fun testRootDependencyNestedPackageJsonConfig() = doTest(expectedMode = PrettierConfiguration.ConfigurationMode.AUTOMATIC)

  @Test
  fun testSubPackageDisabled() = doTest(expectedMode = PrettierConfiguration.ConfigurationMode.DISABLED)

  @Test
  fun testSubPackageDisabledNoPrettier() = doTest(expectedMode = PrettierConfiguration.ConfigurationMode.DISABLED)

  private fun doTest(expectedMode: PrettierConfiguration.ConfigurationMode) {
    prettier.configureAndRun()
    val service = PrettierConfiguration.getInstance(prettier.fixture.project)

    runInEdtAndWait {
      assertEquals(expectedMode, service.configurationMode)
    }
  }
}