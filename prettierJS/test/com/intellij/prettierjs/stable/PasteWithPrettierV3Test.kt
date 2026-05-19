// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.stable

import com.intellij.lang.javascript.modules.TestNpmPackage
import com.intellij.lang.javascript.pasteAndWaitJSCopyPasteService
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.prettierjs.PrettierConfiguration
import com.intellij.prettierjs.PrettierConfiguration.ConfigurationMode
import com.intellij.prettierjs.PrettierJSTestUtil
import java.awt.datatransfer.StringSelection

@TestNpmPackage(PRETTIER_3_8_1_TEST_PACKAGE_SPEC)
class PasteWithPrettierV3Test : PrettierPackageLockTest() {

  override fun setUp() {
    super.setUp()
    myFixture.testDataPath = PrettierJSTestUtil.getTestDataPath() + "paste"
  }

  fun testRunPrettierOnPaste() = withInstallation {
    configureRunOnPaste({
                          val configuration = PrettierConfiguration.getInstance(project)
                          configuration.state.configurationMode = ConfigurationMode.AUTOMATIC
                        }) { doTestPasteAction() }
  }

  fun testRunPrettierOnPasteManual() = withInstallation {
    configureRunOnPaste({
                          val configuration = PrettierConfiguration.getInstance(project)
                          configuration.state.configurationMode = ConfigurationMode.MANUAL
                          configuration.state.runOnReformat = false
                        }) { doTestPasteAction() }
  }

  fun testRunPrettierOnPasteWithAutoImport() = withInstallation {
    configureRunOnPaste({
                          val configuration = PrettierConfiguration.getInstance(project)
                          configuration.state.configurationMode = ConfigurationMode.AUTOMATIC
                        }) {
      val dirName = getTestName(true)
      myFixture.configureFromTempProjectFile("source.js")
      myFixture.performEditorAction(IdeActions.ACTION_EDITOR_COPY)
      myFixture.configureFromTempProjectFile("toReformat.js")
      myFixture.pasteAndWaitJSCopyPasteService()
      myFixture.checkResultByFile("$dirName/toReformat_after.js")
    }
  }

  private fun configureRunOnPaste(configure: Runnable, runnable: Runnable) {
    val configuration = PrettierConfiguration.getInstance(project)
    val runOnPaste = configuration.state.runOnPaste
    val configurationMode = configuration.state.configurationMode

    configuration.state.runOnPaste = true
    configure.run()

    // Warm up the Prettier language service (starts Node.js process) before paste action.
    // Without this, the service cold start on Windows can cause formatting to fail.
    warmUpPrettierService(myFixture.findFileInTempDir("package.json") ?: myFixture.tempDirFixture.getFile(".")!!)

    try {
      // Test data already copied by withInstallation
      myFixture.tempDirFixture.copyAll(getNodePackage().systemIndependentPath, "node_modules/prettier")

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
    val dirName = getTestName(true)
    myFixture.configureFromTempProjectFile("toReformat.js")

    // Set up clipboard with content to paste
    CopyPasteManager.getInstance().setContents(StringSelection(textToPaste))
    myFixture.pasteAndWaitJSCopyPasteService()
    // Check result
    myFixture.checkResultByFile("$dirName/toReformat_after.js")
  }
}
