// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.ide.trustedProjects.TrustedFiles
import com.intellij.ide.trustedProjects.TrustedProjects
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiManager
import com.intellij.testFramework.HeavyPlatformTestCase
import com.intellij.testFramework.TrustedProjectsTestUtil
import java.nio.file.Files

/**
 * Checks that Prettier does not format an untrusted (safe-mode) file.
 * A file opened from an external source outside the project roots is untrusted
 * until the user trusts its location.
 */
class PrettierSafeModeTest : HeavyPlatformTestCase() {
  // a file-based test project sits directly in the temp root, so every temp file
  // would lie inside its roots; the file under test must stay outside
  override fun isCreateDirectoryBasedProject(): Boolean = true

  override fun setUp() {
    super.setUp()
    TrustedProjectsTestUtil.enableTrustedProjectsCheck(testRootDisposable)
    Registry.get(TrustedFiles.SAFE_MODE_REGISTRY_KEY).setValue(true, testRootDisposable)
    // the MANUAL mode opens the non-trust gates of the formatting scope check
    PrettierConfiguration.getInstance(project).state.configurationMode = PrettierConfiguration.ConfigurationMode.MANUAL
  }

  fun testUntrustedFileBlocksFormattingUntilItsPathIsTrusted() {
    val outsidePath = tempDir.createDir().resolve("app.ts")
    Files.writeString(outsidePath, "let   a =    1")
    val file = requireNotNull(LocalFileSystem.getInstance().refreshAndFindFileByNioFile(outsidePath))
    TrustedFiles.markExternallyOpened(file)

    assertFalse(TrustedFiles.isTrusted(file, project))
    assertFalse(isPrettierFormattingAllowedFor(project, file, checkIsInContent = false))

    // the funnel of every format request must fail fast, before any service or config work
    val psiFile = requireNotNull(PsiManager.getInstance(project).findFile(file))
    val future = ReformatWithPrettierAction.performRequestForFileAsync(psiFile, null, null)
    assertTrue("the request must complete immediately, without a service start", future.isDone)
    assertEquals(PrettierBundle.message("prettier.safe.mode.error"), requireNotNull(future.get()).error)

    TrustedProjects.setProjectTrusted(outsidePath, true)
    assertTrue(TrustedFiles.isTrusted(file, project))
    assertTrue(isPrettierFormattingAllowedFor(project, file, checkIsInContent = false))
  }
}
