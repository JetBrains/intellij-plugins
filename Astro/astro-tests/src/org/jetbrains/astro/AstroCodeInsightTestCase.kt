package org.jetbrains.astro

import com.intellij.javascript.testFramework.web.WebFrameworkTestCase

abstract class AstroCodeInsightTestCase(override val testCasePath: String) : WebFrameworkTestCase() {

  override val testDataRoot: String
    get() = getAstroTestDataPath()

  override val defaultDependencies: Map<String, String> =
    mapOf("astro" to "1.9.0")

  override val defaultExtension: String
    get() = "astro"

}