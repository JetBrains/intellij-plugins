package org.jetbrains.qodana

import com.intellij.testFramework.fixtures.BasePlatformTestCase

abstract class QodanaPluginLightTestBase:
  BasePlatformTestCase(),
  QodanaPluginTest by QodanaPluginTestImpl() {

  override fun getBasePath() = "/contrib/qodana/core/test-data"

  override fun setUp() {
    super.setUp()
    init(
      { myFixture },
      { testRootDisposable }
    )
  }

  override fun tearDown() {
    try {
      tearDownQodanaTest()
    }
    catch (e: Throwable) {
      addSuppressedException(e)
    }
    finally {
      super.tearDown()
    }
  }
}