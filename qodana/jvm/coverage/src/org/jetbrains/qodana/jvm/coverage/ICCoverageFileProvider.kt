package org.jetbrains.qodana.jvm.coverage

import com.intellij.coverage.CoverageFileProvider
import java.io.File

/*
 * Coverage file provider for JVM coverage that won't trigger opening up a tool window with coverage.
 * Only coverage highlighting will be shown by default.
 */
class ICCoverageFileProvider(private val file: File) : CoverageFileProvider {
  override fun getCoverageDataFilePath(): String = file.path
  override fun ensureFileExists(): Boolean = file.exists()
  override fun isValid(): Boolean = ensureFileExists()
}