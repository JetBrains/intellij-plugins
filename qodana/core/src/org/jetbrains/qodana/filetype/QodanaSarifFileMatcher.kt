package org.jetbrains.qodana.filetype

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.application

interface QodanaSarifFileMatcher {
  fun isSarifFile(file: VirtualFile): Boolean

  fun getSarifExtensions(): Array<String>

  companion object {
    private const val SARIF = "sarif"
    private const val JSON = "json"

    private val SARIF_JSON_FILE_PATTERN = Regex(".*\\.$SARIF.*\\.$JSON", RegexOption.IGNORE_CASE)

    private fun getInstanceOrNull(): QodanaSarifFileMatcher? = application.getService(QodanaSarifFileMatcher::class.java)

    fun isSarifFile(file: VirtualFile): Boolean =
      getInstanceOrNull()?.isSarifFile(file) ?: isSarifReportFilename(file.name)

    fun getSarifExtensions(): Array<String> =
      getInstanceOrNull()?.getSarifExtensions() ?: getDefaultSarifExtensions()

    fun getDefaultSarifExtensions(): Array<String> = arrayOf(SARIF, JSON)

    fun isSarifReportFilename(fileName: String): Boolean {
      return fileName.endsWith(".$SARIF", ignoreCase = true) || SARIF_JSON_FILE_PATTERN.matches(fileName)
    }
  }
}