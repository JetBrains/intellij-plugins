// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.stable

import com.intellij.lang.javascript.JSTestUtils
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.prettierjs.stable.PrettierPackageLockFixture.Companion.prettierPackageLockFixture
import com.intellij.testFramework.runInEdtAndWait
import com.intellij.util.LineSeparator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Generic base for Prettier formatting tests across different Prettier versions.
 *
 * Subclasses must be annotated with [@TestNpmPackage][com.intellij.lang.javascript.modules.TestNpmPackage].
 */
abstract class ReformatWithPrettierGenericTest {
  protected val prettier by prettierPackageLockFixture()

  // Basic Formatting Tests - Most likely to catch version incompatibilities

  @Test
  fun testWithoutConfig(): Unit = prettier.withInstallation {
    prettier.doReformatFile("js")
  }

  @Test
  fun testTypeScriptWithoutConfig(): Unit = prettier.withInstallation {
    prettier.doReformatFile("ts")
  }

  @Test
  fun testWithPackageJsonConfig(): Unit = prettier.withInstallation {
    prettier.doReformatFile("js")
  }

  // File Detection Tests

  @Test
  fun testJsonFileDetectedByExtension(): Unit = prettier.withInstallation {
    prettier.doReformatFile("json")
  }

  @Test
  fun testFileDetectedByShebangLine(): Unit = prettier.withInstallation {
    prettier.doReformatFile("test", "")
  }

  // Config Resolution Tests - Config parsing may change between versions

  @Test
  fun testResolveConfig(): Unit = prettier.withInstallation {
    prettier.doReformatFile("webc")
  }

  @Test
  fun testWithEditorConfig(): Unit = prettier.withInstallation {
    prettier.doReformatFile("js")
  }

  // Error Handling Tests - Error message format often changes

  @Test
  fun testInvalidConfigErrorReported(): Unit = prettier.withInstallation {
    prettier.assertError({ s -> s.contains("tabWidth") }) { prettier.doReformatFile("js") }
  }

  @Test
  fun testNotSupportedFile(): Unit = prettier.withInstallation {
    prettier.assertError({ s -> s.contains("unsupported type") }) { prettier.doReformatFile("test", "txt") }
  }

  // Caret Position Tests - formatWithCursor API may change

  @Test
  fun testCaretPositionReformat(): Unit = prettier.withInstallation {
    prettier.configureRunOnReformat { prettier.doTestEditorReformat("") }
  }

  // Line Separator Tests

  @Test
  fun testWithCrlf(): Unit = prettier.withInstallation {
    prettier.doReformatFile<Throwable>("toReformat", "js") {
      JSTestUtils.ensureLineSeparators(prettier.fixture.file, LineSeparator.CRLF)
    }
    runInEdtAndWait { FileDocumentManager.getInstance().saveAllDocuments() }
    assertEquals(LineSeparator.CRLF, StringUtil.detectSeparators(VfsUtilCore.loadText(prettier.file.virtualFile)))
  }

  // Ignored File Tests

  @Test
  fun testIgnoredFile(): Unit = prettier.withInstallation {
    prettier.doReformatFile("toReformat", "js")
  }

  // On-Save Tests

  @Test
  fun testRunPrettierOnSaveDocument(): Unit = prettier.withInstallation {
    prettier.configureRunOnSave { prettier.doTestSaveAction("SaveDocument", "") }
  }

  // On-Reformat Tests

  @Test
  fun testRunPrettierOnCodeReformat(): Unit = prettier.withInstallation {
    prettier.configureRunOnReformat { prettier.doTestEditorReformat("") }
  }
}
