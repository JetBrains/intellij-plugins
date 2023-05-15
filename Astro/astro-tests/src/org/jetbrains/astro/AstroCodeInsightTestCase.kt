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

  protected fun configure(fileContents: String? = null,
                          dir: Boolean = false,
                          additionalFiles: List<String> = emptyList(),
                          vararg modules: AstroTestModule) {
    if (dir) {
      myFixture.copyDirectoryToProject(getTestName(true), ".")
    }
    else if (additionalFiles.isNotEmpty()) {
      myFixture.configureByFiles(*additionalFiles.toTypedArray())
    }
    if (modules.isNotEmpty()) {
      myFixture.configureAstroDependencies(*modules)
    }
    if (fileContents != null) {
      myFixture.configureByText(getTestName(true) + ".astro", fileContents)
    }
    else if (dir) {
      myFixture.configureFromTempProjectFile(getTestName(true) + ".astro")
    }
    else {
      myFixture.configureByFile(getTestName(true) + ".astro")
    }
  }

  protected fun checkResult() {
    myFixture.checkResultByFile(getTestName(true) + "_after.astro")
  }

}