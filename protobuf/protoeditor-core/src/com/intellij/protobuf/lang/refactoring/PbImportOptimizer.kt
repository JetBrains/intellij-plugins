package com.intellij.protobuf.lang.refactoring

import com.intellij.lang.ImportOptimizer
import com.intellij.openapi.util.EmptyRunnable
import com.intellij.protobuf.lang.psi.PbFile
import com.intellij.protobuf.lang.psi.PbImportStatement
import com.intellij.protobuf.lang.resolve.PbImportReference
import com.intellij.psi.PsiFile
import com.intellij.util.castSafelyTo

internal class PbImportOptimizer : ImportOptimizer {
  override fun supports(file: PsiFile): Boolean {
    return file is PbFile
  }

  override fun processFile(file: PsiFile): Runnable {
    if (file !is PbFile || file.importStatements.isEmpty()) return EmptyRunnable.INSTANCE

    val originalGoodImports = mutableSetOf<PbFile>()

    val duplicates = mutableSetOf<PbImportStatement>()
    for (importStatement in file.importStatements) {
      val importedFile = importStatement.importName?.references
                           ?.filterIsInstance<PbImportReference>()
                           ?.singleOrNull()?.resolve()
                           ?.castSafelyTo<PbFile>() ?: continue
      if (importedFile in originalGoodImports) {
        duplicates.add(importStatement)
      }
      else {
        originalGoodImports.add(importedFile)
      }
    }

    return Runnable { removeDuplicatedImports(file, duplicates) }
  }

  private fun removeDuplicatedImports(file: PbFile, duplicates: Set<PbImportStatement>) {
    for (importStatement in file.importStatements) {
      if (importStatement in duplicates) {
        importStatement.delete()
      }
    }
  }
}