package com.intellij.protobuf.lang.refactoring

import com.intellij.protobuf.lang.psi.PbFile
import com.intellij.protobuf.lang.psi.PbImportStatement
import com.intellij.protobuf.lang.resolve.PbImportReference
import com.intellij.util.castSafelyTo

internal fun collectDuplicatedImportsSkipOriginal(file: PbFile): Collection<PbImportStatement> {
  return analyzeImports(file).second.values.flatten()
}

internal fun collectDuplicatedImportsIncludeOriginal(file: PbFile): Collection<PbImportStatement> {
  val (originalImports, duplicatesWithoutOriginal) = analyzeImports(file)
  val allImportsWithDuplicates = mutableListOf<PbImportStatement>()

  for (duplicate in duplicatesWithoutOriginal) {
    allImportsWithDuplicates += duplicate.value
    val duplicatedImport = originalImports[duplicate.key]
    if (duplicatedImport != null) {
      allImportsWithDuplicates += duplicatedImport
    }
  }

  return allImportsWithDuplicates
}

private fun analyzeImports(file: PbFile): Pair<Map<PbFile, PbImportStatement>, Map<PbFile, List<PbImportStatement>>> {
  val originalGoodImports = mutableMapOf<PbFile, PbImportStatement>()

  val duplicatesWithoutOriginal = mutableMapOf<PbFile, MutableList<PbImportStatement>>()
  for (importStatement in file.importStatements) {
    val importedFile = importStatement.importName?.references
                         ?.filterIsInstance<PbImportReference>()
                         ?.singleOrNull()?.resolve()
                         ?.castSafelyTo<PbFile>() ?: continue
    if (importedFile in originalGoodImports) {
      if (duplicatesWithoutOriginal[importedFile] == null) {
        duplicatesWithoutOriginal[importedFile] = mutableListOf()
      }
      duplicatesWithoutOriginal[importedFile]?.add(importStatement)
    }
    else {
      originalGoodImports[importedFile] = importStatement
    }
  }

  return originalGoodImports to duplicatesWithoutOriginal
}

internal fun removeDuplicatedImports(file: PbFile, duplicates: Set<PbImportStatement>) {
  for (importStatement in file.importStatements) {
    if (importStatement in duplicates) {
      importStatement.delete()
    }
  }
}