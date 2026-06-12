package com.intellij.openRewrite

import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase

abstract class OpenRewriteLightHighlightingTestCase : LightJavaCodeInsightFixtureTestCase() {
  override fun getProjectDescriptor(): LightProjectDescriptor {
    return OpenRewriteProjectDescriptor()
  }

  protected open fun getPluginTestDataRoot(): String = "/plugins/open-rewrite/testData/"

  protected open fun getTestDirectory(): String = "Override_getTestDirectory"

  override fun getBasePath(): String = getPluginTestDataRoot() + getTestDirectory()
}