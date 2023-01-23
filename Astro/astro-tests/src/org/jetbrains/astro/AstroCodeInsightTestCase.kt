package org.jetbrains.astro

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.webSymbols.enableAstLoadingFilter

abstract class AstroCodeInsightTestCase : BasePlatformTestCase() {
  @Throws(Exception::class)
  override fun setUp() {
    super.setUp()
    enableAstLoadingFilter()
  }

  override fun getTestDataPath(): String = getAstroTestDataPath() + "/" + basePath

}