// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.lang.javascript.linter.JSExternalToolIntegrationTest
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.prettierjs.PrettierConfiguration.ConfigurationMode
import com.intellij.testFramework.EditorTestUtil
import java.awt.datatransfer.StringSelection

class PasteWithPrettierTest : JSExternalToolIntegrationTest() {

  override fun getMainPackageName(): String {
    return PrettierUtil.PACKAGE_NAME
  }

  override fun setUp() {
    super.setUp()
    myFixture.setTestDataPath(PrettierJSTestUtil.getTestDataPath() + "paste")
  }

  fun testRunPrettierOnPaste() {
    configureRunOnPaste({
                          val configuration = PrettierConfiguration.getInstance(project)
                          configuration.state.configurationMode = ConfigurationMode.AUTOMATIC
                        }) { doTestPasteAction() }
  }

  fun testRunPrettierOnPasteManual() {
    configureRunOnPaste({
                          val configuration = PrettierConfiguration.getInstance(project)
                          configuration.state.configurationMode = ConfigurationMode.MANUAL
                          configuration.state.runOnReformat = false
                        }) { doTestPasteAction() }
  }

  private fun configureRunOnPaste(configure: Runnable, runnable: Runnable) {
    val configuration = PrettierConfiguration.getInstance(project)
    val runOnPaste = configuration.state.runOnPaste
    val configurationMode = configuration.state.configurationMode

    configuration.state.runOnPaste = true
    configure.run()

    try {
      val dirName = getTestName(true)
      myFixture.copyDirectoryToProject(dirName, "")
      myFixture.getTempDirFixture().copyAll(nodePackage.systemIndependentPath, "node_modules/prettier")

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

    // Perform paste action
    EditorTestUtil.performPaste(myFixture.getEditor())

    // Check result
    myFixture.checkResultByFile("$dirName/toReformat_after.js")
  }
}
