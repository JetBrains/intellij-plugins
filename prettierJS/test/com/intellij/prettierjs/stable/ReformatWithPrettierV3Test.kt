// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.stable

import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.modules.TestNpmPackage
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.prettierjs.PrettierConfiguration
import com.intellij.testFramework.utils.ActionsOnSaveTestUtil
import com.intellij.util.LineSeparator
import java.nio.file.Files

@TestNpmPackage(PRETTIER_3_8_1_TEST_PACKAGE_SPEC)
class ReformatWithPrettierV3Test : ReformatWithPrettierGenericTest() {

  // Additional Basic Formatting Tests

  fun testTypeScriptWithEmptyConfig() = withInstallation {
    doReformatFile("ts")
  }

  fun testJsFileWithSelection() = withInstallation {
    doReformatFile("js")
  }

  // Additional File Detection Tests

  fun testJsonFileDetectedByName() = withInstallation {
    doReformatFile(".babelrc", "")
  }

  // Additional Ignored File Tests

  fun testSubFolderIgnoredFileInRoot() = withInstallation {
    doReformatFile("package/toReformat", "js")
  }

  fun testSubFolderIgnoredFileInsidePackage() = withInstallation {
    doReformatFile("package/toReformat", "js")
  }

  fun testSubFolderIgnoredFileInsideSubDir() = withInstallation {
    doReformatFile("package/subdir/toReformat", "js")
  }

  fun testSubFolderIgnoredFileManual() = withInstallation {
    doReformatFile<Throwable>("package/toReformat", "js") {
      PrettierConfiguration.getInstance(project).state.configurationMode =
        PrettierConfiguration.ConfigurationMode.MANUAL
    }
  }

  fun testSubFolderIgnoredFileManualSubDir() = withInstallation {
    doReformatFile<Throwable>("package/toReformat", "js") {
      val config = PrettierConfiguration.getInstance(project)
      config.state.configurationMode = PrettierConfiguration.ConfigurationMode.MANUAL

      val ignoreFile = myFixture.findFileInTempDir(".prettierignore")
      config.state.customIgnorePath = ignoreFile.toNioPath().toString()
    }
  }

  fun testSubFolderIgnoredManualFormat() = withInstallation {
    doReformatFile<Throwable>("package/toReformat", "js") {
      val config = PrettierConfiguration.getInstance(project)
      config.state.configurationMode = PrettierConfiguration.ConfigurationMode.MANUAL

      val ignoreFile = myFixture.findFileInTempDir(".prettierignore")
      config.state.customIgnorePath = ignoreFile.toNioPath().toString()
    }
  }

  // Additional Line Separator Tests

  fun testWithUpdatingLfToCrlf() = withInstallation {
    doReformatFile("toReformat", "js")
    FileDocumentManager.getInstance().saveAllDocuments()
    assertEquals(LineSeparator.CRLF, StringUtil.detectSeparators(VfsUtilCore.loadText(getFile().virtualFile)))
  }

  // Additional Caret Position Tests

  fun testCaretPosition() = withInstallation {
    configureRunOnSave {
      val actionId = "SaveDocument"
      doTestSaveAction(actionId, "")
    }
  }

  fun testCaretPositionEndFileReformat() = withInstallation {
    configureRunOnReformat { doTestEditorReformat("") }
  }

  fun testCaretPositionReformatParenthesis() = withInstallation {
    configureRunOnReformat { doTestEditorReformat("") }
  }

  fun testCrlfCaretPosition() = withInstallation {
    configureRunOnSave {
      val actionId = "SaveDocument"
      doTestSaveAction(actionId, "") {
        JSTestUtils.ensureLineSeparators(myFixture.file, LineSeparator.CRLF)
      }
    }
  }

  fun testCrlfCaretPositionReformat() = withInstallation {
    configureRunOnReformat {
      doTestEditorReformat("") {
        JSTestUtils.ensureLineSeparators(myFixture.file, LineSeparator.CRLF)
      }
    }
  }

  // Patch Tests

  fun testPatchApplied() = withInstallation {
    configureRunOnSave {
      val actionId = "SaveDocument"
      doTestSaveAction(actionId, "first/")
      doTestSaveAction(actionId, "second/")
    }
  }

  fun testPatchAppliedDeletion() = withInstallation {
    doReformatFile("js")
  }

  fun testPatchAppliedEndline() = withInstallation {
    doReformatFile("js")
  }

  // Additional On-Save Tests

  fun testRunPrettierOnSaveAll() = withInstallation {
    configureRunOnSave { doTestSaveAction("SaveAll", "") }
  }

  // Additional On-Reformat Tests

  fun testCommentAfterImports() = withInstallation {
    configureRunOnReformat { doTestEditorReformat("") }
  }

  fun testCommentAfterStatement() = withInstallation {
    configureRunOnReformat { doTestEditorReformat("") }
  }

  // Monorepo Tests

  fun testMonorepoIndirectDependency() = withInstallation {
    configureRunOnSave {
      val actionId = "SaveDocument"

      //file in the root without prettier
      doTestSaveAction(actionId, "")

      //package with an indirect dependecy prettier in subfolder
      doTestSaveAction(actionId, "package-a/")

      //package with prettier in subfolder
      doTestSaveAction(actionId, "package-b/")

      //package with prettier config in subfolder
      doTestSaveAction(actionId, "package-c/")
    }
  }

  fun testMonorepoManualScopeOnSave() = withInstallation {
    configureFormatFilesOutsideDependencyScope(false) {
      val actionId = "SaveDocument"

      //file in the root without prettier
      doTestSaveAction(actionId, "")

      //package with prettier in subfolder
      doTestSaveAction(actionId, "package-a/")

      //package without prettier in subfolder
      doTestSaveAction(actionId, "package-b/")
    }
  }

  fun testMonorepoManualScopeReformat() = withInstallation {
    configureFormatFilesOutsideDependencyScope(false) {
      //file in the root without prettier
      doTestEditorReformat("")

      //package with prettier in subfolder
      doTestEditorReformat("package-a/")

      //package without prettier in subfolder
      doTestEditorReformat("package-b/")
    }
  }

  fun testMonorepoManualWithoutScope() = withInstallation {
    configureFormatFilesOutsideDependencyScope(true) {
      val actionId = "SaveDocument"

      //file in the root without prettier
      doTestSaveAction(actionId, "")
      doTestEditorReformat("")

      //package with prettier in subfolder
      doTestSaveAction(actionId, "package-a/")
      doTestEditorReformat("package-a/")

      //package without prettier in subfolder
      doTestSaveAction(actionId, "package-b/")
      doTestEditorReformat("package-b/")
    }
  }

  fun testMonorepoOnSave() = withInstallation {
    configureRunOnSave {
      val actionId = "SaveDocument"

      //file in the root without prettier
      doTestSaveAction(actionId, "")

      //package with prettier in subfolder
      doTestSaveAction(actionId, "package-a/")

      //package without prettier in subfolder
      doTestSaveAction(actionId, "package-b/")
    }
  }

  fun testMonorepoSubDirEditorReformat() = withInstallation {
    configureRunOnReformat {
      //file in the root without prettier
      doTestEditorReformat("")

      //package with prettier in subfolder
      doTestEditorReformat("package-a/")

      //package without prettier in subfolder
      doTestEditorReformat("package-b/")
    }
  }

  fun testMonorepoSubDirOnSave() = withInstallation {
    configureRunOnSave {
      val actionId = "SaveDocument"

      //file in the root without prettier
      doTestSaveAction(actionId, "")

      //package with prettier in subfolder
      doTestSaveAction(actionId, "package-a/")

      //package without prettier in subfolder
      doTestSaveAction(actionId, "package-b/")
    }
  }

  fun testMonorepoSubDirReformatAction() = withInstallation {
    // file in the root without prettier but it should be formatted via reformat action
    val dirName = getTestName(true)
    // Test data already copied by withInstallation
    myFixture.configureFromExistingVirtualFile(myFixture.findFileInTempDir("toReformat.js"))
    runReformatAction()
    myFixture.checkResultByFile("$dirName/toReformat_after.js")
  }

  // Special Cases

  fun testAutoconfigured() = withSubdirInstallation("autoconfigured", "subdir") {
    configureMode(PrettierConfiguration.ConfigurationMode.AUTOMATIC) {
      // Test that file in subdir (with package.json) is formatted
      myFixture.configureFromExistingVirtualFile(myFixture.findFileInTempDir("subdir/formatted.js"))
      runReformatAction()
      myFixture.checkResultByFile("autoconfigured/subdir/formatted_after.js")

      // Test that file at root (outside prettier scope) is NOT formatted
      myFixture.configureFromExistingVirtualFile(myFixture.findFileInTempDir("ignored.js"))
      runReformatAction()
      myFixture.checkResultByFile("autoconfigured/ignored_after.js")
    }
  }

  fun testChangeConfig() = withInstallation {
    val dirName = getTestName(true)
    // Test data already copied by withInstallation
    myFixture.configureFromExistingVirtualFile(myFixture.findFileInTempDir("toReformat_after.js"))

    // set singleQuote to true
    val config = myFixture.createFile("prettier.config.mjs", """
      const config = {
        singleQuote: true,
      }

      export default config
      """.trimIndent())
    runReformatAction()
    myFixture.checkResultByFile("$dirName/toReformat_after.js")

    // change singleQuote to false
    myFixture.saveText(config, """
      const config = {
        singleQuote: false,
      }

      export default config
      """.trimIndent())
    runReformatAction()
    myFixture.checkResultByFile("$dirName/toReformat_after_1.js")
  }

  fun testChangeConfigByRename() = withInstallation {
    val dirName = getTestName(true)
    // Test data already copied by withInstallation
    myFixture.configureFromExistingVirtualFile(myFixture.findFileInTempDir("toReformat.js"))

    // the file does not have a config file name yet, so Prettier uses its defaults
    val config = myFixture.createFile("prettier.config.txt", """
      const config = {
        singleQuote: true,
      }

      export default config
      """.trimIndent())
    runReformatAction()
    myFixture.checkResultByFile("$dirName/toReformat_after.js")

    // the rename gives the file a config file name
    WriteAction.run<Throwable> { config.rename(this, "prettier.config.mjs") }
    runReformatAction()
    myFixture.checkResultByFile("$dirName/toReformat_after_1.js")

    // the rename removes the config file name
    WriteAction.run<Throwable> { config.rename(this, "prettier.config.txt") }
    runReformatAction()
    myFixture.checkResultByFile("$dirName/toReformat_after.js")
  }

  fun testChangeConfigByDelete() = withInstallation {
    val dirName = getTestName(true)
    // Test data already copied by withInstallation
    myFixture.configureFromExistingVirtualFile(myFixture.findFileInTempDir("toReformat.js"))
    runReformatAction()
    myFixture.checkResultByFile("$dirName/toReformat_after.js")

    // without the config file, Prettier uses its defaults
    WriteAction.run<Throwable> { myFixture.findFileInTempDir(".prettierrc.json").delete(this) }
    runReformatAction()
    myFixture.checkResultByFile("$dirName/toReformat_after_1.js")
  }

  fun testChangePackageJsonConfig() = withInstallation {
    val dirName = getTestName(true)
    // Test data already copied by withInstallation
    myFixture.configureFromExistingVirtualFile(myFixture.findFileInTempDir("toReformat.js"))
    runReformatAction()
    myFixture.checkResultByFile("$dirName/toReformat_after.js")

    // change singleQuote to false in the "prettier" property of package.json
    val packageJson = myFixture.findFileInTempDir("package.json")
    val text = VfsUtilCore.loadText(packageJson)
    assertTrue(text.contains(""""singleQuote": true"""))
    myFixture.saveText(packageJson, text.replace(""""singleQuote": true""", """"singleQuote": false"""))
    runReformatAction()
    myFixture.checkResultByFile("$dirName/toReformat_after_1.js")
  }

  fun testChangeEditorConfig() = withInstallation {
    val dirName = getTestName(true)
    // Test data already copied by withInstallation
    myFixture.configureFromExistingVirtualFile(myFixture.findFileInTempDir("toReformat.js"))
    runReformatAction()
    myFixture.checkResultByFile("$dirName/toReformat_after.js")

    // change the indentation from 4 spaces to a tab
    myFixture.saveText(myFixture.findFileInTempDir(".editorconfig"), "root = true\n\n[*]\nindent_style = tab\n")
    runReformatAction()
    myFixture.checkResultByFile("$dirName/toReformat_after_1.js")
  }

  fun testChangeConfigBySavingDocument() = withInstallation {
    val dirName = getTestName(true)
    // Test data already copied by withInstallation
    myFixture.configureFromExistingVirtualFile(myFixture.findFileInTempDir("toReformat.js"))
    runReformatAction()
    myFixture.checkResultByFile("$dirName/toReformat_after.js")

    // the user changes the config in the editor and saves it
    val configDocument = disableSingleQuoteInConfigDocument()
    FileDocumentManager.getInstance().saveDocument(configDocument)
    runReformatAction()
    myFixture.checkResultByFile("$dirName/toReformat_after_1.js")
  }

  fun testChangeConfigInUnsavedDocument() = withInstallation {
    val dirName = getTestName(true)
    // Test data already copied by withInstallation
    myFixture.configureFromExistingVirtualFile(myFixture.findFileInTempDir("toReformat.js"))
    runReformatAction()
    myFixture.checkResultByFile("$dirName/toReformat_after.js")

    // the user changes the config in the editor and does not save it
    val configDocument = disableSingleQuoteInConfigDocument()
    // the action saves the config before it gets a service, so the save must terminate the old service first
    runReformatAction()
    assertFalse(FileDocumentManager.getInstance().isDocumentUnsaved(configDocument))
    myFixture.checkResultByFile("$dirName/toReformat_after_1.js")
  }

  fun testChangeConfigByRefresh() = withInstallation {
    val dirName = getTestName(true)
    // Test data already copied by withInstallation
    myFixture.configureFromExistingVirtualFile(myFixture.findFileInTempDir("toReformat.js"))
    runReformatAction()
    myFixture.checkResultByFile("$dirName/toReformat_after.js")

    // another program changes the config file, and a refresh finds the change because the length changes
    val config = myFixture.findFileInTempDir(".prettierrc.json")
    Files.writeString(config.toNioPath(), """{"singleQuote": false}""")
    VfsUtil.markDirtyAndRefresh(false, false, false, config)
    runReformatAction()
    myFixture.checkResultByFile("$dirName/toReformat_after_1.js")
  }

  fun testIncompleteBlock() = withInstallation {
    val configuration = PrettierConfiguration.getInstance(project)
    val origRunOnReformat = configuration.state.runOnReformat
    configuration.state.runOnReformat = true
    try {
      val dirName = getTestName(true)
      // Test data already copied by withInstallation
      myFixture.configureFromTempProjectFile("toReformat.js")
      // should be used exactly ACTION_EDITOR_REFORMAT instead of ReformatWithPrettierAction
      // to check a default formatter behavior combined with Prettier
      myFixture.performEditorAction(IdeActions.ACTION_EDITOR_REFORMAT)
      myFixture.checkResultByFile("$dirName/toReformat_after.js")
    } finally {
      configuration.state.runOnReformat = origRunOnReformat
    }
  }

  fun testRangeInVue() = withInstallation {
    // Prettier doesn't support range formatting in Vue (WEB-52196, https://github.com/prettier/prettier/issues/13399),
    // and even removes line break at the end of the file. This test checks IDE's workaround of Prettier bug.
    doReformatFile("foo", "vue")
  }

  fun testGracefulFallbackCursor() = withInstallation {
    doReformatFile("toReformat", "html")
  }

  fun testCaretPositionReformatSvelte() = withInstallation {
    doReformatFile("toReformat", "svelte")
  }

  fun testOptimizeImportsAndPrettierOnSave() = withInstallation {
    configureRunOptimizeImportsAndPrettierOnSave {
      val dirName = getTestName(true)
      myFixture.configureFromTempProjectFile("toReformat.js")
      myFixture.type(' ')
      myFixture.performEditorAction("SaveDocument")
      ActionsOnSaveTestUtil.waitForActionsOnSaveToFinish(myFixture.project)
      myFixture.checkResultByFile("$dirName/toReformat_after.js")
    }
  }

  /** Sets `singleQuote` to `false` in the `.prettierrc.json` document and does not save it. */
  private fun disableSingleQuoteInConfigDocument(): Document {
    val document = FileDocumentManager.getInstance().getDocument(myFixture.findFileInTempDir(".prettierrc.json"))!!
    WriteCommandAction.runWriteCommandAction(project) { document.setText("""{"singleQuote": false}""") }
    assertTrue(FileDocumentManager.getInstance().isDocumentUnsaved(document))
    return document
  }
}
