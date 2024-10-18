// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.openapi.util.Ref
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.runInEdtAndGet
import kotlinx.coroutines.runBlocking

abstract class PrettierConfigurationTestBase : BasePlatformTestCase() {

  override fun runInDispatchThread() = false

  override fun setUp() {
    super.setUp()
    myFixture.testDataPath = PrettierJSTestUtil.getTestDataPath() + "configuration"
  }

  override fun tearDown() {
    try {
      PrettierConfiguration.getInstance(myFixture.project).state.configurationMode = null
    }
    catch (e: Throwable) {
      addSuppressedException(e)
    }
    finally {
      super.tearDown()
    }
  }

  protected fun configureAndRun() {
    val dirName = getTestName(true)
    val newPath = runInEdtAndGet {
      myFixture.copyDirectoryToProject(dirName, "")
    }
    val configurator = PrettierProjectConfigurator()

    runBlocking {
      configurator.configure(myFixture.project, newPath, Ref(myFixture.module), false)
    }
  }
}