// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.stable

import com.intellij.lang.javascript.modules.TestNpmPackage
import com.intellij.lang.javascript.pasteAndWaitJSCopyPasteService
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.prettierjs.PrettierConfiguration
import com.intellij.prettierjs.PrettierConfiguration.ConfigurationMode
import com.intellij.prettierjs.stable.PrettierPackageLockFixture.Companion.prettierPackageLockFixture
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.runInEdtAndWait
import org.junit.jupiter.api.Test
import java.awt.datatransfer.StringSelection

@TestApplication
@TestNpmPackage(PRETTIER_3_8_1_TEST_PACKAGE_SPEC)
class PasteWithPrettierV3Test {
  private val prettier by prettierPackageLockFixture("paste")

  @Test
  fun testRunPrettierOnPaste(): Unit = prettier.withInstallation {
    configureRunOnPaste({
                          val configuration = PrettierConfiguration.getInstance(prettier.project)
                          configuration.state.configurationMode = ConfigurationMode.AUTOMATIC
                        }) { doTestPasteAction() }
  }

  @Test
  fun testRunPrettierOnPasteManual(): Unit = prettier.withInstallation {
    configureRunOnPaste({
                          val configuration = PrettierConfiguration.getInstance(prettier.project)
                          configuration.state.configurationMode = ConfigurationMode.MANUAL
                          configuration.state.runOnReformat = false
                        }) { doTestPasteAction() }
  }

  @Test
  fun testRunPrettierOnPasteWithAutoImport(): Unit = prettier.withInstallation {
    configureRunOnPaste({
                          val configuration = PrettierConfiguration.getInstance(prettier.project)
                          configuration.state.configurationMode = ConfigurationMode.AUTOMATIC
                        }) {
      val dirName = prettier.getTestName(true)
      prettier.fixture.configureFromTempProjectFile("source.js")
      runInEdtAndWait { prettier.fixture.performEditorAction(IdeActions.ACTION_EDITOR_COPY) }
      prettier.fixture.configureFromTempProjectFile("toReformat.js")
      runInEdtAndWait { prettier.fixture.pasteAndWaitJSCopyPasteService() }
      prettier.fixture.checkResultByFile("$dirName/toReformat_after.js")
    }
  }

  @Test
  fun testRunPrettierOnPasteInsidePrettierIgnoredClass(): Unit = prettier.withInstallation {
    configureRunOnPaste({
                          val configuration = PrettierConfiguration.getInstance(prettier.project)
                          configuration.state.configurationMode = ConfigurationMode.AUTOMATIC
                        }) {
      doTestPasteAction("    /** AAA_003 */   bbb_002  =  String(Index({ unique: true, isUUID:true }))\n")
    }
  }

  private fun configureRunOnPaste(configure: Runnable, runnable: Runnable) {
    val configuration = PrettierConfiguration.getInstance(prettier.project)
    val runOnPaste = configuration.state.runOnPaste
    val configurationMode = configuration.state.configurationMode

    configuration.state.runOnPaste = true
    configure.run()

    // Warm up the Prettier language service (starts Node.js process) before paste action.
    // Without this, the service cold start on Windows can cause formatting to fail.
    prettier.warmUpPrettierService(prettier.fixture.findFileInTempDir("package.json") ?: prettier.fixture.tempDirFixture.getFile(".")!!)

    try {
      // Test data already copied by withInstallation
      prettier.fixture.tempDirFixture.copyAll(prettier.nodePackage.systemIndependentPath, "node_modules/prettier")

      runnable.run()
    }
    finally {
      configuration.state.runOnPaste = runOnPaste
      configuration.state.configurationMode = configurationMode
    }
  }

  private fun doTestPasteAction() {
    doTestPasteAction("""
      [
           1, 2, 3, 4, 5, 6,
           7, 8, 9, 10, 11, 12, 13, 14, 123,
      ].forEach((x) => console.log(x));
    """.trimIndent())
  }

  private fun doTestPasteAction(textToPaste: String) {
    val dirName = prettier.getTestName(true)
    prettier.fixture.configureFromTempProjectFile("toReformat.js")

    // Set up clipboard with content to paste
    CopyPasteManager.getInstance().setContents(StringSelection(textToPaste))
    runInEdtAndWait { prettier.fixture.pasteAndWaitJSCopyPasteService() }
    // Check result
    prettier.fixture.checkResultByFile("$dirName/toReformat_after.js")
  }
}
