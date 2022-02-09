package com.intellij.protobuf.lang.refactoring

import com.intellij.lang.ImportOptimizer
import com.intellij.openapi.util.EmptyRunnable
import com.intellij.protobuf.lang.psi.PbFile
import com.intellij.psi.PsiFile

internal class PbImportOptimizer : ImportOptimizer {
  override fun supports(file: PsiFile): Boolean {
    return file is PbFile
  }

  override fun processFile(file: PsiFile): Runnable {
    if (file !is PbFile || file.importStatements.isEmpty()) return EmptyRunnable.INSTANCE
    val duplicates = collectDuplicatedImportsSkipOriginal(file)
    return Runnable { removeDuplicatedImports(file, duplicates.toSet()) }
  }
}