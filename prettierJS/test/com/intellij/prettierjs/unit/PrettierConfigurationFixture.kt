// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.unit

import com.intellij.openapi.util.Ref
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightProjectFixture
import com.intellij.prettierjs.PrettierConfiguratorService
import com.intellij.prettierjs.PrettierJSTestUtil
import com.intellij.prettierjs.PrettierProjectConfigurator
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.junit5.fixture.TestFixture
import com.intellij.testFramework.junit5.fixture.testFixture
import com.intellij.testFramework.runInEdtAndGet
import com.intellij.testFramework.utils.coroutines.waitCoroutinesBlocking
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.TestOnly

/**
 * Creates a [PrettierConfigurationFixture] with a new project for each test.
 * The test data path is `testData/configuration`.
 */
@TestOnly
fun prettierConfigurationFixture(): TestFixture<PrettierConfigurationFixture> =
  testFixture("prettierConfigurationFixture") { context ->
    val fixture = codeInsightFixture(codeInsightProjectFixture()).init()
    fixture.testDataPath = PrettierJSTestUtil.getTestDataPath() + "configuration"
    initialized(PrettierConfigurationFixture(fixture, context.extensionContext.requiredTestMethod.name)) {}
  }

/**
 * Runs [PrettierProjectConfigurator] on a copy of the test data directory of the test.
 */
@TestOnly
class PrettierConfigurationFixture(val fixture: CodeInsightTestFixture, private val testMethodName: String) {
  /**
   * Copies the test data directory that has the name of the test into the project, and configures Prettier for it.
   * The method waits until the coroutines of [PrettierConfiguratorService] complete.
   */
  fun configureAndRun() {
    val dirName = PlatformTestUtil.getTestName(testMethodName, true)
    val newPath = runInEdtAndGet {
      fixture.copyDirectoryToProject(dirName, "")
    }
    val configurator = PrettierProjectConfigurator()

    runBlocking {
      configurator.configure(fixture.project, newPath, Ref(fixture.module), false)
    }

    waitCoroutinesBlocking(fixture.project.getService(PrettierConfiguratorService::class.java).cs)
  }
}
