// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.stable

import com.intellij.codeInsight.template.impl.TemplateManagerImpl
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.modules.TestNpmPackage
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.prettierjs.PrettierConfiguration
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.runInEdtAndWait
import com.intellij.testFramework.utils.ActionsOnSaveTestUtil
import com.intellij.util.LineSeparator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files

@TestNpmPackage(PRETTIER_3_8_1_TEST_PACKAGE_SPEC)
@TestApplication
class ReformatWithPrettierV3Test : ReformatWithPrettierGenericTest() {

  // Additional Basic Formatting Tests

  @Test
  fun testTypeScriptWithEmptyConfig(): Unit = prettier.withInstallation {
    prettier.doReformatFile("ts")
  }

  @Test
  fun testJsFileWithSelection(): Unit = prettier.withInstallation {
    prettier.doReformatFile("js")
  }

  @Test
  fun testJsFileWithSelectionInsidePrettierIgnoredClass(): Unit = prettier.withInstallation {
    prettier.doReformatFile("js")
  }

  @Test
  fun testJsFileWithSelectionInsidePrettierIgnoredClassCrlf(): Unit = prettier.withInstallation {
    prettier.doReformatFile<Throwable>("toReformat", "js") {
      JSTestUtils.ensureLineSeparators(prettier.fixture.file, LineSeparator.CRLF)
    }
    runInEdtAndWait { FileDocumentManager.getInstance().saveAllDocuments() }
    assertEquals(LineSeparator.CRLF, StringUtil.detectSeparators(VfsUtilCore.loadText(prettier.file.virtualFile)))
  }

  @Test
  fun testRunPrettierOnCodeReformatSelectionInsidePrettierIgnoredClass(): Unit = prettier.withInstallation {
    prettier.configureRunOnReformat { prettier.doTestEditorReformat("") }
  }

  // Additional File Detection Tests

  @Test
  fun testJsonFileDetectedByName(): Unit = prettier.withInstallation {
    prettier.doReformatFile(".babelrc", "")
  }

  // Additional Ignored File Tests

  @Test
  fun testSubFolderIgnoredFileInRoot(): Unit = prettier.withInstallation {
    prettier.doReformatFile("package/toReformat", "js")
  }

  @Test
  fun testSubFolderIgnoredFileInsidePackage(): Unit = prettier.withInstallation {
    prettier.doReformatFile("package/toReformat", "js")
  }

  @Test
  fun testSubFolderIgnoredFileInsideSubDir(): Unit = prettier.withInstallation {
    prettier.doReformatFile("package/subdir/toReformat", "js")
  }

  @Test
  fun testSubFolderIgnoredFileManual(): Unit = prettier.withInstallation {
    prettier.doReformatFile<Throwable>("package/toReformat", "js") {
      PrettierConfiguration.getInstance(prettier.project).state.configurationMode =
        PrettierConfiguration.ConfigurationMode.MANUAL
    }
  }

  @Test
  fun testSubFolderIgnoredFileManualSubDir(): Unit = prettier.withInstallation {
    prettier.doReformatFile<Throwable>("package/toReformat", "js") {
      val config = PrettierConfiguration.getInstance(prettier.project)
      config.state.configurationMode = PrettierConfiguration.ConfigurationMode.MANUAL

      val ignoreFile = prettier.fixture.findFileInTempDir(".prettierignore")
      config.state.customIgnorePath = ignoreFile.toNioPath().toString()
    }
  }

  @Test
  fun testSubFolderIgnoredManualFormat(): Unit = prettier.withInstallation {
    prettier.doReformatFile<Throwable>("package/toReformat", "js") {
      val config = PrettierConfiguration.getInstance(prettier.project)
      config.state.configurationMode = PrettierConfiguration.ConfigurationMode.MANUAL

      val ignoreFile = prettier.fixture.findFileInTempDir(".prettierignore")
      config.state.customIgnorePath = ignoreFile.toNioPath().toString()
    }
  }

  // Additional Line Separator Tests

  @Test
  fun testWithUpdatingLfToCrlf(): Unit = prettier.withInstallation {
    prettier.doReformatFile("toReformat", "js")
    runInEdtAndWait { FileDocumentManager.getInstance().saveAllDocuments() }
    assertEquals(LineSeparator.CRLF, StringUtil.detectSeparators(VfsUtilCore.loadText(prettier.file.virtualFile)))
  }

  // Additional Caret Position Tests

  @Test
  fun testCaretPosition(): Unit = prettier.withInstallation {
    prettier.configureRunOnSave {
      val actionId = "SaveDocument"
      prettier.doTestSaveAction(actionId, "")
    }
  }

  @Test
  fun testCaretPositionEndFileReformat(): Unit = prettier.withInstallation {
    prettier.configureRunOnReformat { prettier.doTestEditorReformat("") }
  }

  @Test
  fun testCaretPositionReformatParenthesis(): Unit = prettier.withInstallation {
    prettier.configureRunOnReformat { prettier.doTestEditorReformat("") }
  }

  @Test
  fun testCrlfCaretPosition(): Unit = prettier.withInstallation {
    prettier.configureRunOnSave {
      val actionId = "SaveDocument"
      prettier.doTestSaveAction(actionId, "") {
        JSTestUtils.ensureLineSeparators(prettier.fixture.file, LineSeparator.CRLF)
      }
    }
  }

  @Test
  fun testCrlfCaretPositionReformat(): Unit = prettier.withInstallation {
    prettier.configureRunOnReformat {
      prettier.doTestEditorReformat("") {
        JSTestUtils.ensureLineSeparators(prettier.fixture.file, LineSeparator.CRLF)
      }
    }
  }

  // Patch Tests

  @Test
  fun testPatchApplied(): Unit = prettier.withInstallation {
    prettier.configureRunOnSave {
      val actionId = "SaveDocument"
      prettier.doTestSaveAction(actionId, "first/")
      prettier.doTestSaveAction(actionId, "second/")
    }
  }

  @Test
  fun testPatchAppliedDeletion(): Unit = prettier.withInstallation {
    prettier.doReformatFile("js")
  }

  @Test
  fun testPatchAppliedEndline(): Unit = prettier.withInstallation {
    prettier.doReformatFile("js")
  }

  // Additional On-Save Tests

  @Test
  fun testRunPrettierOnSaveAll(): Unit = prettier.withInstallation {
    prettier.configureRunOnSave { prettier.doTestSaveAction("SaveAll", "") }
  }

  // Additional On-Reformat Tests

  @Test
  fun testCommentAfterImports(): Unit = prettier.withInstallation {
    prettier.configureRunOnReformat { prettier.doTestEditorReformat("") }
  }

  @Test
  fun testCommentAfterStatement(): Unit = prettier.withInstallation {
    prettier.configureRunOnReformat { prettier.doTestEditorReformat("") }
  }

  // Postfix Template Tests

  @Test
  fun testPostfixIfTemplate(): Unit = prettier.withInstallation {
    prettier.configureRunOnReformat {
      // The code style modifier waits for Prettier with a timeout.
      // The test turns the modifier off, so the indentation does not depend on the timeout.
      val state = PrettierConfiguration.getInstance(prettier.project).state
      val codeStyleSettingsModifierEnabled = state.codeStyleSettingsModifierEnabled
      state.codeStyleSettingsModifierEnabled = false
      try {
        TemplateManagerImpl.setTemplateTesting(prettier.testRootDisposable)
        prettier.fixture.configureFromTempProjectFile("toReformat.js")
        runInEdtAndWait { prettier.fixture.type('\t') }
        prettier.fixture.checkResultByFile("${prettier.getTestName(true)}/toReformat_after.js", true)
      }
      finally {
        state.codeStyleSettingsModifierEnabled = codeStyleSettingsModifierEnabled
      }
    }
  }

  // Monorepo Tests

  @Test
  fun testMonorepoIndirectDependency(): Unit = prettier.withInstallation {
    prettier.configureRunOnSave {
      val actionId = "SaveDocument"

      //file in the root without prettier
      prettier.doTestSaveAction(actionId, "")

      //package with an indirect dependecy prettier in subfolder
      prettier.doTestSaveAction(actionId, "package-a/")

      //package with prettier in subfolder
      prettier.doTestSaveAction(actionId, "package-b/")

      //package with prettier config in subfolder
      prettier.doTestSaveAction(actionId, "package-c/")
    }
  }

  @Test
  fun testMonorepoManualScopeOnSave(): Unit = prettier.withInstallation {
    prettier.configureFormatFilesOutsideDependencyScope(false) {
      val actionId = "SaveDocument"

      //file in the root without prettier
      prettier.doTestSaveAction(actionId, "")

      //package with prettier in subfolder
      prettier.doTestSaveAction(actionId, "package-a/")

      //package without prettier in subfolder
      prettier.doTestSaveAction(actionId, "package-b/")
    }
  }

  @Test
  fun testMonorepoManualScopeReformat(): Unit = prettier.withInstallation {
    prettier.configureFormatFilesOutsideDependencyScope(false) {
      //file in the root without prettier
      prettier.doTestEditorReformat("")

      //package with prettier in subfolder
      prettier.doTestEditorReformat("package-a/")

      //package without prettier in subfolder
      prettier.doTestEditorReformat("package-b/")
    }
  }

  @Test
  fun testMonorepoManualWithoutScope(): Unit = prettier.withInstallation {
    prettier.configureFormatFilesOutsideDependencyScope(true) {
      val actionId = "SaveDocument"

      //file in the root without prettier
      prettier.doTestSaveAction(actionId, "")
      prettier.doTestEditorReformat("")

      //package with prettier in subfolder
      prettier.doTestSaveAction(actionId, "package-a/")
      prettier.doTestEditorReformat("package-a/")

      //package without prettier in subfolder
      prettier.doTestSaveAction(actionId, "package-b/")
      prettier.doTestEditorReformat("package-b/")
    }
  }

  @Test
  fun testMonorepoOnSave(): Unit = prettier.withInstallation {
    prettier.configureRunOnSave {
      val actionId = "SaveDocument"

      //file in the root without prettier
      prettier.doTestSaveAction(actionId, "")

      //package with prettier in subfolder
      prettier.doTestSaveAction(actionId, "package-a/")

      //package without prettier in subfolder
      prettier.doTestSaveAction(actionId, "package-b/")
    }
  }

  @Test
  fun testMonorepoSubDirEditorReformat(): Unit = prettier.withInstallation {
    prettier.configureRunOnReformat {
      //file in the root without prettier
      prettier.doTestEditorReformat("")

      //package with prettier in subfolder
      prettier.doTestEditorReformat("package-a/")

      //package without prettier in subfolder
      prettier.doTestEditorReformat("package-b/")
    }
  }

  @Test
  fun testMonorepoSubDirOnSave(): Unit = prettier.withInstallation {
    prettier.configureRunOnSave {
      val actionId = "SaveDocument"

      //file in the root without prettier
      prettier.doTestSaveAction(actionId, "")

      //package with prettier in subfolder
      prettier.doTestSaveAction(actionId, "package-a/")

      //package without prettier in subfolder
      prettier.doTestSaveAction(actionId, "package-b/")
    }
  }

  @Test
  fun testMonorepoSubDirReformatAction(): Unit = prettier.withInstallation {
    // file in the root without prettier but it should be formatted via reformat action
    val dirName = prettier.getTestName(true)
    // Test data already copied by withInstallation
    prettier.fixture.configureFromExistingVirtualFile(prettier.fixture.findFileInTempDir("toReformat.js"))
    prettier.runReformatAction()
    prettier.fixture.checkResultByFile("$dirName/toReformat_after.js")
  }

  @Test
  fun testMonorepoSubPackagePlugin(): Unit = prettier.withInstallation {
    // npm hoists every package into the root `node_modules`. The move gives `package-a` the plugin that only it depends on.
    prettier.moveInstalledPackage("prettier-plugin-svelte", "package-a")
    prettier.doReformatFile("package-a/toReformat", "js")
  }

  // Special Cases

  @Test
  fun testAutoconfigured(): Unit = prettier.withSubdirInstallation("autoconfigured", "subdir") {
    prettier.configureMode(PrettierConfiguration.ConfigurationMode.AUTOMATIC) {
      // Test that file in subdir (with package.json) is formatted
      prettier.fixture.configureFromExistingVirtualFile(prettier.fixture.findFileInTempDir("subdir/formatted.js"))
      prettier.runReformatAction()
      prettier.fixture.checkResultByFile("autoconfigured/subdir/formatted_after.js")

      // Test that file at root (outside prettier scope) is NOT formatted
      prettier.fixture.configureFromExistingVirtualFile(prettier.fixture.findFileInTempDir("ignored.js"))
      prettier.runReformatAction()
      prettier.fixture.checkResultByFile("autoconfigured/ignored_after.js")
    }
  }

  @Test
  fun testChangeConfig(): Unit = prettier.withInstallation {
    val dirName = prettier.getTestName(true)
    // Test data already copied by withInstallation
    prettier.fixture.configureFromExistingVirtualFile(prettier.fixture.findFileInTempDir("toReformat_after.js"))

    // set singleQuote to true
    val config = prettier.fixture.createFile("prettier.config.mjs", """
      const config = {
        singleQuote: true,
      }

      export default config
      """.trimIndent())
    prettier.runReformatAction()
    prettier.fixture.checkResultByFile("$dirName/toReformat_after.js")

    // change singleQuote to false
    prettier.fixture.saveText(config, """
      const config = {
        singleQuote: false,
      }

      export default config
      """.trimIndent())
    prettier.runReformatAction()
    prettier.fixture.checkResultByFile("$dirName/toReformat_after_1.js")
  }

  @Test
  fun testChangeConfigByRename(): Unit = prettier.withInstallation {
    val dirName = prettier.getTestName(true)
    // Test data already copied by withInstallation
    prettier.fixture.configureFromExistingVirtualFile(prettier.fixture.findFileInTempDir("toReformat.js"))

    // the file does not have a config file name yet, so Prettier uses its defaults
    val config = prettier.fixture.createFile("prettier.config.txt", """
      const config = {
        singleQuote: true,
      }

      export default config
      """.trimIndent())
    prettier.runReformatAction()
    prettier.fixture.checkResultByFile("$dirName/toReformat_after.js")

    // the rename gives the file a config file name
    WriteAction.runAndWait<Throwable> { config.rename(this, "prettier.config.mjs") }
    prettier.runReformatAction()
    prettier.fixture.checkResultByFile("$dirName/toReformat_after_1.js")

    // the rename removes the config file name
    WriteAction.runAndWait<Throwable> { config.rename(this, "prettier.config.txt") }
    prettier.runReformatAction()
    prettier.fixture.checkResultByFile("$dirName/toReformat_after.js")
  }

  @Test
  fun testChangeConfigByDelete(): Unit = prettier.withInstallation {
    val dirName = prettier.getTestName(true)
    // Test data already copied by withInstallation
    prettier.fixture.configureFromExistingVirtualFile(prettier.fixture.findFileInTempDir("toReformat.js"))
    prettier.runReformatAction()
    prettier.fixture.checkResultByFile("$dirName/toReformat_after.js")

    // without the config file, Prettier uses its defaults
    WriteAction.runAndWait<Throwable> { prettier.fixture.findFileInTempDir(".prettierrc.json").delete(this) }
    prettier.runReformatAction()
    prettier.fixture.checkResultByFile("$dirName/toReformat_after_1.js")
  }

  @Test
  fun testChangePackageJsonConfig(): Unit = prettier.withInstallation {
    val dirName = prettier.getTestName(true)
    // Test data already copied by withInstallation
    prettier.fixture.configureFromExistingVirtualFile(prettier.fixture.findFileInTempDir("toReformat.js"))
    prettier.runReformatAction()
    prettier.fixture.checkResultByFile("$dirName/toReformat_after.js")

    // change singleQuote to false in the "prettier" property of package.json
    val packageJson = prettier.fixture.findFileInTempDir("package.json")
    val text = VfsUtilCore.loadText(packageJson)
    assertTrue(text.contains(""""singleQuote": true"""))
    prettier.fixture.saveText(packageJson, text.replace(""""singleQuote": true""", """"singleQuote": false"""))
    prettier.runReformatAction()
    prettier.fixture.checkResultByFile("$dirName/toReformat_after_1.js")
  }

  @Test
  fun testChangeEditorConfig(): Unit = prettier.withInstallation {
    val dirName = prettier.getTestName(true)
    // Test data already copied by withInstallation
    prettier.fixture.configureFromExistingVirtualFile(prettier.fixture.findFileInTempDir("toReformat.js"))
    prettier.runReformatAction()
    prettier.fixture.checkResultByFile("$dirName/toReformat_after.js")

    // change the indentation from 4 spaces to a tab
    prettier.fixture.saveText(prettier.fixture.findFileInTempDir(".editorconfig"), "root = true\n\n[*]\nindent_style = tab\n")
    prettier.runReformatAction()
    prettier.fixture.checkResultByFile("$dirName/toReformat_after_1.js")
  }

  @Test
  fun testChangeConfigBySavingDocument(): Unit = prettier.withInstallation {
    val dirName = prettier.getTestName(true)
    // Test data already copied by withInstallation
    prettier.fixture.configureFromExistingVirtualFile(prettier.fixture.findFileInTempDir("toReformat.js"))
    prettier.runReformatAction()
    prettier.fixture.checkResultByFile("$dirName/toReformat_after.js")

    // the user changes the config in the editor and saves it
    val configDocument = disableSingleQuoteInConfigDocument()
    runInEdtAndWait { FileDocumentManager.getInstance().saveDocument(configDocument) }
    prettier.runReformatAction()
    prettier.fixture.checkResultByFile("$dirName/toReformat_after_1.js")
  }

  @Test
  fun testChangeConfigInUnsavedDocument(): Unit = prettier.withInstallation {
    val dirName = prettier.getTestName(true)
    // Test data already copied by withInstallation
    prettier.fixture.configureFromExistingVirtualFile(prettier.fixture.findFileInTempDir("toReformat.js"))
    prettier.runReformatAction()
    prettier.fixture.checkResultByFile("$dirName/toReformat_after.js")

    // the user changes the config in the editor and does not save it
    val configDocument = disableSingleQuoteInConfigDocument()
    // the action saves the config before it gets a service, so the save must terminate the old service first
    prettier.runReformatAction()
    assertFalse(FileDocumentManager.getInstance().isDocumentUnsaved(configDocument))
    prettier.fixture.checkResultByFile("$dirName/toReformat_after_1.js")
  }

  @Test
  fun testChangeConfigByRefresh(): Unit = prettier.withInstallation {
    val dirName = prettier.getTestName(true)
    // Test data already copied by withInstallation
    prettier.fixture.configureFromExistingVirtualFile(prettier.fixture.findFileInTempDir("toReformat.js"))
    prettier.runReformatAction()
    prettier.fixture.checkResultByFile("$dirName/toReformat_after.js")

    // another program changes the config file, and a refresh finds the change because the length changes
    val config = prettier.fixture.findFileInTempDir(".prettierrc.json")
    Files.writeString(config.toNioPath(), """{"singleQuote": false}""")
    VfsUtil.markDirtyAndRefresh(false, false, false, config)
    prettier.runReformatAction()
    prettier.fixture.checkResultByFile("$dirName/toReformat_after_1.js")
  }

  @Test
  fun testIncompleteBlock(): Unit = prettier.withInstallation {
    val configuration = PrettierConfiguration.getInstance(prettier.project)
    val origRunOnReformat = configuration.state.runOnReformat
    configuration.state.runOnReformat = true
    try {
      val dirName = prettier.getTestName(true)
      // Test data already copied by withInstallation
      prettier.fixture.configureFromTempProjectFile("toReformat.js")
      // should be used exactly ACTION_EDITOR_REFORMAT instead of ReformatWithPrettierAction
      // to check a default formatter behavior combined with Prettier
      runInEdtAndWait { prettier.fixture.performEditorAction(IdeActions.ACTION_EDITOR_REFORMAT) }
      prettier.fixture.checkResultByFile("$dirName/toReformat_after.js")
    } finally {
      configuration.state.runOnReformat = origRunOnReformat
    }
  }

  @Test
  fun testRangeInVue(): Unit = prettier.withInstallation {
    // Prettier doesn't support range formatting in Vue (WEB-52196, https://github.com/prettier/prettier/issues/13399),
    // and even removes line break at the end of the file. This test checks IDE's workaround of Prettier bug.
    prettier.doReformatFile("foo", "vue")
  }

  @Test
  fun testGracefulFallbackCursor(): Unit = prettier.withInstallation {
    prettier.doReformatFile("toReformat", "html")
  }

  @Test
  fun testCaretPositionReformatSvelte(): Unit = prettier.withInstallation {
    prettier.doReformatFile("toReformat", "svelte")
  }

  @Test
  fun testOptimizeImportsAndPrettierOnSave(): Unit = prettier.withInstallation {
    prettier.configureRunOptimizeImportsAndPrettierOnSave {
      val dirName = prettier.getTestName(true)
      prettier.fixture.configureFromTempProjectFile("toReformat.js")
      runInEdtAndWait {
        prettier.fixture.type(' ')
        prettier.fixture.performEditorAction("SaveDocument")
        ActionsOnSaveTestUtil.waitForActionsOnSaveToFinish(prettier.fixture.project)
      }
      prettier.fixture.checkResultByFile("$dirName/toReformat_after.js")
    }
  }

  /** Sets `singleQuote` to `false` in the `.prettierrc.json` document and does not save it. */
  private fun disableSingleQuoteInConfigDocument(): Document {
    val configFile = prettier.fixture.findFileInTempDir(".prettierrc.json")
    val document = runReadAction { FileDocumentManager.getInstance().getDocument(configFile) }!!
    WriteCommandAction.runWriteCommandAction(prettier.project) { document.setText("""{"singleQuote": false}""") }
    assertTrue(FileDocumentManager.getInstance().isDocumentUnsaved(document))
    return document
  }
}
