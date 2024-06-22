// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.refactoring

import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.refactoring.RefactoringHelper
import com.intellij.usageView.UsageInfo
import org.intellij.prisma.lang.psi.PrismaElement
import org.intellij.prisma.lang.psi.PrismaFile

class PrismaFormatRefactoringHelper : RefactoringHelper<Collection<PrismaFile>> {
  override fun prepareOperation(usages: Array<out UsageInfo>, elements: List<PsiElement>): Collection<PrismaFile> {
    if (elements.none { it is PrismaElement }) {
      return emptyList()
    }

    return sequenceOf(usages.mapNotNull { it.element }, elements)
      .flatten()
      .map { it.containingFile }
      .filterIsInstance<PrismaFile>()
      .distinct()
      .toList()
  }

  override fun performOperation(project: Project, operationData: Collection<PrismaFile>?) {
    val codeStyleManager = CodeStyleManager.getInstance(project)
    operationData?.forEach {
      if (it.virtualFile.isWritable) {
        runWriteAction { codeStyleManager.reformat(it) }
      }
    }
  }
}