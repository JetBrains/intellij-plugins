package org.jetbrains.qodana

import com.intellij.testFramework.builders.ModuleFixtureBuilder
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase
import com.intellij.testFramework.fixtures.ModuleFixture

abstract class QodanaPluginHeavyTestBase:
  CodeInsightFixtureTestCase<ModuleFixtureBuilder<ModuleFixture>>(),
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