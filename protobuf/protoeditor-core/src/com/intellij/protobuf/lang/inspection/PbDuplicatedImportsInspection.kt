package com.intellij.protobuf.lang.inspection

import com.intellij.codeInspection.*
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.project.Project
import com.intellij.protobuf.lang.PbLangBundle
import com.intellij.protobuf.lang.psi.PbFile
import com.intellij.protobuf.lang.refactoring.collectDuplicatedImportsIncludeOriginal
import com.intellij.protobuf.lang.refactoring.collectDuplicatedImportsSkipOriginal
import com.intellij.protobuf.lang.refactoring.removeDuplicatedImports
import com.intellij.psi.PsiFile

internal class PbDuplicatedImportsInspection : LocalInspectionTool() {
  override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
    if (file !is PbFile || file.importStatements.isEmpty()) return emptyArray()

    return collectDuplicatedImportsIncludeOriginal(file)
      .asSequence()
      .mapNotNull { it.importName }
      .map {
        manager.createProblemDescriptor(
          it,
          PbLangBundle.message("inspection.duplicated.imports.name"),
          arrayOf(object : LocalQuickFix {
            override fun getFamilyName(): String {
              return PbLangBundle.message("inspection.duplicated.imports.optimize.imports.fix.name")
            }

            override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
              val duplicates = collectDuplicatedImportsSkipOriginal(file)
              runWriteAction {
                removeDuplicatedImports(file, duplicates.toSet())
              }
            }
          }),
          ProblemHighlightType.GENERIC_ERROR,
          isOnTheFly,
          false)
      }.toList().toTypedArray()
  }
}