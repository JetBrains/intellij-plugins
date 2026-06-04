package org.jetbrains.qodana.json

import com.intellij.json.JsonFileType
import com.intellij.openapi.fileTypes.ExtensionFileNameMatcher
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.qodana.filetype.QodanaSarifFileMatcher

internal class QodanaSarifFileMatcherImpl : QodanaSarifFileMatcher {
  override fun isSarifFile(file: VirtualFile): Boolean =
    FileTypeManager.getInstance().isFileOfType(file, SarifFileType)

  override fun getSarifExtensions(): Array<String> {
    return listOf(SarifFileType, JsonFileType.INSTANCE).flatMap {
      FileTypeManager.getInstance().getAssociations(it).filterIsInstance<ExtensionFileNameMatcher>()
    }.map { it.extension }.toTypedArray()
  }
}
