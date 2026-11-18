// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.ide.TrustedFiles
import com.intellij.ide.trustedProjects.TrustedProjects
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiManager
import com.intellij.testFramework.TrustedProjectsTestUtil
import com.intellij.testFramework.junit5.RegistryKey
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.disposableFixture
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.junit5.fixture.tempPathFixture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Files

/**
 * Checks that Prettier does not format an untrusted (safe-mode) file.
 * A file opened from an external source outside the project roots is untrusted
 * until the user trusts its location.
 */
@TestApplication
@RegistryKey(key = TrustedFiles.SAFE_MODE_REGISTRY_KEY, value = "true")
class PrettierSafeModeTest {
  private val projectFixture = projectFixture(openAfterCreation = true)
  private val project get() = projectFixture.get()
  private val testRootDisposable by disposableFixture()
  private val outsideDir by tempPathFixture()

  @BeforeEach
  fun setUp() {
    TrustedProjectsTestUtil.enableTrustedProjectsCheck(testRootDisposable)
    // the MANUAL mode opens the non-trust gates of the formatting scope check
    PrettierConfiguration.getInstance(project).state.configurationMode = PrettierConfiguration.ConfigurationMode.MANUAL
  }

  @Test
  fun testUntrustedFileBlocksFormattingUntilItsPathIsTrusted() {
    val outsidePath = outsideDir.resolve("app.ts")
    Files.writeString(outsidePath, "let   a =    1")
    val file = requireNotNull(VirtualFileManager.getInstance().refreshAndFindFileByNioPath(outsidePath))
    TrustedFiles.markExternallyOpened(file)

    assertFalse(TrustedFiles.isTrusted(file, project))
    assertFalse(isPrettierFormattingAllowedFor(project, file, checkIsInContent = false))

    // the funnel of every format request must fail fast, before any service or config work
    val psiFile = runReadActionBlocking { requireNotNull(PsiManager.getInstance(project).findFile(file)) }
    val future = ReformatWithPrettierAction.performRequestForFileAsync(psiFile, null, null)
    assertTrue(future.isDone, "the request must complete immediately, without a service start")
    assertEquals(PrettierBundle.message("prettier.safe.mode.error"), requireNotNull(future.get()).error)

    TrustedProjects.setProjectTrusted(outsidePath, true)
    assertTrue(TrustedFiles.isTrusted(file, project))
    assertTrue(isPrettierFormattingAllowedFor(project, file, checkIsInContent = false))
  }
}
