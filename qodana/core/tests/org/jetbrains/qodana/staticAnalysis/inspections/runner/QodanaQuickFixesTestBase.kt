package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.application.options.CodeStyle
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.util.io.toCanonicalPath
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.readText
import org.jetbrains.qodana.staticAnalysis.inspections.config.FixesStrategy
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig


/**
 * Tests of generating fixes functionality.
 */
abstract class QodanaQuickFixesTestBase(private val strategy: FixesStrategy) : QodanaRunnerTestCase() {
  private var oldIndent: Int? = null
  override fun setUp() {
    super.setUp()

    updateQodanaConfig {
      it.copy(
        fixesStrategy = strategy
      )
    }
    val settings = CodeStyle.getSettings(project)
    oldIndent =  settings.getCommonSettings(JavaLanguage.INSTANCE).indentOptions?.INDENT_SIZE
    settings.getCommonSettings(JavaLanguage.INSTANCE).indentOptions?.INDENT_SIZE = 2
  }

  override fun tearDown() {
    try {
      CodeStyle.getSettings(project).getCommonSettings(JavaLanguage.INSTANCE).indentOptions?.INDENT_SIZE = oldIndent!!
    }
    catch (e: Throwable) {
      addSuppressedException(e)
    }
    finally {
      super.tearDown()
    }
  }

  fun runTest(profileName: String) {
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig(name = profileName)
      )
    }
    analyzeAndValidateResults()
  }

  fun runTestWithProfilePath(profilePath: String) {
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig(path = profilePath),
      )
    }
    analyzeAndValidateResults()
  }

  private fun analyzeAndValidateResults() {
    runAnalysis()
    assertSarifResults()
    assertFixes()
  }

  private fun assertFixes() {
    val run = manager.qodanaRunner.sarifRun
    val afterFixesPath = getTestDataPath("afterFixes")

    val projectDir = VfsUtil.findFile(qodanaConfig.projectPath, false)
    val uris = run.results.map { result -> result.locations[0].physicalLocation.artifactLocation.uri }.distinct()
    uris.forEach { uri ->
      val file = VfsUtil.findRelativeFile(uri, projectDir)!!
      assertSameLinesWithFile(afterFixesPath.resolve(file.name).toCanonicalPath(), file.readText())
    }
  }
}
